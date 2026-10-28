package com.designtouch.backend.service;

import com.designtouch.backend.dto.ServicesHeroBannerRequest;
import com.designtouch.backend.dto.ServicesHeroBannerResponse;
import com.designtouch.backend.dto.ServicesPageContentRequest;
import com.designtouch.backend.dto.ServicesPageContentResponse;
import com.designtouch.backend.entity.ServicesHeroBanner;
import com.designtouch.backend.entity.ServicesPageContent;
import com.designtouch.backend.exception.ResourceNotFoundException;
import com.designtouch.backend.repository.PortfolioItemRepository;
import com.designtouch.backend.repository.ServicesHeroBannerRepository;
import com.designtouch.backend.repository.ServicesPageContentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServicesPageContentService {

    private static final Logger log = LoggerFactory.getLogger(ServicesPageContentService.class);
    public static final int MAX_HERO_BANNERS = 5;

    private final ServicesPageContentRepository repository;
    private final ServicesHeroBannerRepository bannerRepository;
    private final MediaService mediaService;
    private final PortfolioItemRepository portfolioItemRepository;

    @Transactional
    public ServicesPageContentResponse getServicesPageContent() {
        ServicesPageContent content = repository.findSingleton()
                .orElseGet(this::createDefault);
        ensureLegacyBannerMigrated(content);
        List<ServicesHeroBannerResponse> activeBanners = getActiveHeroBanners();
        return ServicesPageContentResponse.from(content, activeBanners);
    }

    @Transactional
    public List<ServicesHeroBannerResponse> getActiveHeroBanners() {
        ServicesPageContent content = repository.findSingleton().orElse(null);
        if (content != null) {
            ensureLegacyBannerMigrated(content);
        }
        return bannerRepository.findAllByIsActiveTrueOrderByDisplayOrderAscCreatedAtAsc()
                .stream()
                .filter(b -> b.getImageUrl() != null && !b.getImageUrl().isBlank())
                .map(ServicesHeroBannerResponse::from)
                .toList();
    }

    @Transactional
    public List<ServicesHeroBannerResponse> getAllHeroBannersAdmin() {
        ServicesPageContent content = repository.findSingleton().orElse(null);
        if (content != null) {
            ensureLegacyBannerMigrated(content);
        }
        return bannerRepository.findAllByOrderByDisplayOrderAscCreatedAtAsc()
                .stream()
                .map(ServicesHeroBannerResponse::from)
                .toList();
    }

    @Transactional
    public ServicesHeroBannerResponse createHeroBanner(ServicesHeroBannerRequest request) {
        ServicesPageContent content = repository.findSingleton().orElse(null);
        if (content != null) {
            ensureLegacyBannerMigrated(content);
        }

        long currentCount = bannerRepository.count();
        if (currentCount >= MAX_HERO_BANNERS) {
            throw new IllegalStateException("Maximum of 5 hero banners allowed. Please delete or replace an existing banner.");
        }

        if (request == null || request.imageUrl() == null || request.imageUrl().isBlank()) {
            throw new IllegalArgumentException("Banner image URL is required.");
        }

        int nextOrder = request.displayOrder() != null ? request.displayOrder() : (int) currentCount + 1;
        String label = (request.label() != null && !request.label().isBlank())
                ? request.label().trim()
                : "Banner " + ((int) currentCount + 1);

        ServicesHeroBanner banner = ServicesHeroBanner.builder()
                .label(label)
                .imageUrl(request.imageUrl().trim())
                .imagePublicId(request.imagePublicId() != null && !request.imagePublicId().isBlank() ? request.imagePublicId().trim() : null)
                .displayOrder(nextOrder)
                .isActive(request.isActive() != null ? request.isActive() : true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        ServicesHeroBanner saved = bannerRepository.saveAndFlush(banner);
        syncPrimaryBannerToPageContent();
        return ServicesHeroBannerResponse.from(saved);
    }

    @Transactional
    public ServicesHeroBannerResponse updateHeroBanner(UUID id, ServicesHeroBannerRequest request) {
        ServicesHeroBanner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hero banner not found: " + id));

        if (request == null || request.imageUrl() == null || request.imageUrl().isBlank()) {
            throw new IllegalArgumentException("Banner image URL is required.");
        }

        String oldPublicId = banner.getImagePublicId();
        String newPublicId = (request.imagePublicId() != null && !request.imagePublicId().isBlank())
                ? request.imagePublicId().trim()
                : null;

        if (request.label() != null && !request.label().isBlank()) {
            banner.setLabel(request.label().trim());
        }
        banner.setImageUrl(request.imageUrl().trim());
        banner.setImagePublicId(newPublicId);
        if (request.displayOrder() != null) {
            banner.setDisplayOrder(request.displayOrder());
        }
        if (request.isActive() != null) {
            banner.setIsActive(request.isActive());
        }
        banner.setUpdatedAt(OffsetDateTime.now());

        // Persist and flush DB update BEFORE deleting any replaced Cloudinary asset
        ServicesHeroBanner saved = bannerRepository.saveAndFlush(banner);
        syncPrimaryBannerToPageContent();

        if (oldPublicId != null && !oldPublicId.isBlank() && !oldPublicId.equals(newPublicId)) {
            deleteCloudinaryAssetIfUnreferenced(oldPublicId);
        }

        return ServicesHeroBannerResponse.from(saved);
    }

    @Transactional
    public ServicesHeroBannerResponse toggleHeroBannerActive(UUID id) {
        ServicesHeroBanner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hero banner not found: " + id));

        boolean current = banner.getIsActive() != null ? banner.getIsActive() : true;
        banner.setIsActive(!current);
        banner.setUpdatedAt(OffsetDateTime.now());

        ServicesHeroBanner saved = bannerRepository.saveAndFlush(banner);
        syncPrimaryBannerToPageContent();
        return ServicesHeroBannerResponse.from(saved);
    }

    @Transactional
    public List<ServicesHeroBannerResponse> reorderHeroBanners(List<UUID> orderedIds) {
        if (orderedIds == null || orderedIds.isEmpty()) {
            return getAllHeroBannersAdmin();
        }
        List<ServicesHeroBanner> all = bannerRepository.findAllByOrderByDisplayOrderAscCreatedAtAsc();
        int order = 1;
        for (UUID targetId : orderedIds) {
            for (ServicesHeroBanner b : all) {
                if (b.getId().equals(targetId)) {
                    b.setDisplayOrder(order++);
                    b.setUpdatedAt(OffsetDateTime.now());
                    break;
                }
            }
        }
        bannerRepository.saveAllAndFlush(all);
        syncPrimaryBannerToPageContent();
        return getAllHeroBannersAdmin();
    }

    @Transactional
    public void deleteHeroBanner(UUID id) {
        ServicesHeroBanner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hero banner not found: " + id));

        String oldPublicId = banner.getImagePublicId();
        bannerRepository.delete(banner);
        bannerRepository.flush();

        // Renumber remaining banners' displayOrder sequentially
        List<ServicesHeroBanner> remaining = bannerRepository.findAllByOrderByDisplayOrderAscCreatedAtAsc();
        int order = 1;
        for (ServicesHeroBanner b : remaining) {
            b.setDisplayOrder(order++);
        }
        bannerRepository.saveAllAndFlush(remaining);
        syncPrimaryBannerToPageContent();

        if (oldPublicId != null && !oldPublicId.isBlank()) {
            deleteCloudinaryAssetIfUnreferenced(oldPublicId);
        }
    }

    @Transactional
    public ServicesPageContentResponse updateServicesPageContent(ServicesPageContentRequest request) {
        ServicesPageContent content = repository.findSingleton()
                .orElseGet(this::createDefault);
        ensureLegacyBannerMigrated(content);

        // Only update/replace the banner image when a non-blank banner URL is provided.
        // Explicit deletion is handled via deleteHeroBannerImage() or deleteHeroBanner(id),
        // ensuring existing Cloudinary URLs and public IDs remain intact when saving other CMS tabs.
        if (request.heroBannerImageUrl() != null && !request.heroBannerImageUrl().isBlank()) {
            String trimmedUrl = request.heroBannerImageUrl().trim();
            String newPublicId = (request.heroBannerImagePublicId() != null && !request.heroBannerImagePublicId().isBlank())
                    ? request.heroBannerImagePublicId().trim()
                    : content.getHeroBannerImagePublicId();
            String oldPublicId = content.getHeroBannerImagePublicId();

            content.setHeroBannerImageUrl(trimmedUrl);
            content.setHeroBannerImagePublicId(newPublicId);

            // Sync with Banner 1 if banner table is empty or Banner 1 matches
            List<ServicesHeroBanner> allBanners = bannerRepository.findAllByOrderByDisplayOrderAscCreatedAtAsc();
            if (allBanners.isEmpty()) {
                ServicesHeroBanner first = ServicesHeroBanner.builder()
                        .label("Banner 1")
                        .imageUrl(trimmedUrl)
                        .imagePublicId(newPublicId)
                        .displayOrder(1)
                        .isActive(true)
                        .createdAt(OffsetDateTime.now())
                        .updatedAt(OffsetDateTime.now())
                        .build();
                bannerRepository.saveAndFlush(first);
            } else {
                ServicesHeroBanner first = allBanners.get(0);
                boolean alreadyInList = allBanners.stream().anyMatch(b -> trimmedUrl.equals(b.getImageUrl()));
                if (!alreadyInList) {
                    first.setImageUrl(trimmedUrl);
                    first.setImagePublicId(newPublicId);
                    first.setUpdatedAt(OffsetDateTime.now());
                    bannerRepository.saveAndFlush(first);
                }
            }

            if (oldPublicId != null && !oldPublicId.isBlank() && !oldPublicId.equals(newPublicId)) {
                deleteCloudinaryAssetIfUnreferenced(oldPublicId);
            }
        }

        content.setHeroEyebrow(request.heroEyebrow());
        content.setHeroHeadlineLine1(request.heroHeadlineLine1());
        content.setHeroHeadlineLine2(request.heroHeadlineLine2());
        content.setHeroHeadlineHighlight(request.heroHeadlineHighlight());
        content.setHeroDescription(request.heroDescription());
        content.setHeroPrimaryCtaText(request.heroPrimaryCtaText());
        content.setHeroPrimaryCtaLink(request.heroPrimaryCtaLink());
        content.setHeroSecondaryCtaText(request.heroSecondaryCtaText());
        content.setHeroSecondaryCtaLink(request.heroSecondaryCtaLink());
        content.setHeroVideoUrl(request.heroVideoUrl());
        content.setHeroStat1Value(request.heroStat1Value());
        content.setHeroStat1Label(request.heroStat1Label());
        content.setHeroStat2Value(request.heroStat2Value());
        content.setHeroStat2Label(request.heroStat2Label());
        content.setHeroStat3Value(request.heroStat3Value());
        content.setHeroStat3Label(request.heroStat3Label());
        if (request.heroShowStats() != null) content.setHeroShowStats(request.heroShowStats());
        if (request.heroShowWorkstation() != null) content.setHeroShowWorkstation(request.heroShowWorkstation());

        content.setServicesEyebrow(request.servicesEyebrow());
        content.setServicesHeading(request.servicesHeading());
        content.setServicesDescription(request.servicesDescription());

        content.setProcessEyebrow(request.processEyebrow());
        content.setProcessHeading(request.processHeading());
        content.setProcessDescription(request.processDescription());

        content.setToolsEyebrow(request.toolsEyebrow());
        content.setToolsHeading(request.toolsHeading());
        content.setToolsDescription(request.toolsDescription());

        content.setCtaEyebrow(request.ctaEyebrow());
        content.setCtaHeadingLine1(request.ctaHeadingLine1());
        content.setCtaHeadingLine2(request.ctaHeadingLine2());
        content.setCtaHeadingHighlight(request.ctaHeadingHighlight());
        content.setCtaDescription(request.ctaDescription());
        content.setCtaButtonText(request.ctaButtonText());
        content.setCtaButtonLink(request.ctaButtonLink());
        content.setCtaAccentNote(request.ctaAccentNote());
        content.setUpdatedAt(OffsetDateTime.now());

        ServicesPageContent saved = repository.saveAndFlush(content);
        return ServicesPageContentResponse.from(saved, getActiveHeroBanners());
    }

    @Transactional
    public ServicesPageContentResponse deleteHeroBannerImage() {
        ServicesPageContent content = repository.findSingleton()
                .orElseGet(this::createDefault);

        String oldPublicId = content.getHeroBannerImagePublicId();
        content.setHeroBannerImageUrl(null);
        content.setHeroBannerImagePublicId(null);
        content.setUpdatedAt(OffsetDateTime.now());
        ServicesPageContent saved = repository.saveAndFlush(content);

        List<ServicesHeroBanner> allBanners = bannerRepository.findAll();
        bannerRepository.deleteAll();
        bannerRepository.flush();

        if (oldPublicId != null && !oldPublicId.isBlank()) {
            deleteCloudinaryAssetIfUnreferenced(oldPublicId);
        }
        for (ServicesHeroBanner b : allBanners) {
            if (b.getImagePublicId() != null && !b.getImagePublicId().isBlank()) {
                deleteCloudinaryAssetIfUnreferenced(b.getImagePublicId());
            }
        }

        return ServicesPageContentResponse.from(saved, List.of());
    }

    private void ensureLegacyBannerMigrated(ServicesPageContent content) {
        if (bannerRepository == null || content == null) return;
        if (bannerRepository.count() == 0
                && content.getHeroBannerImageUrl() != null
                && !content.getHeroBannerImageUrl().isBlank()) {
            ServicesHeroBanner migrated = ServicesHeroBanner.builder()
                    .label("Banner 1")
                    .imageUrl(content.getHeroBannerImageUrl().trim())
                    .imagePublicId(content.getHeroBannerImagePublicId())
                    .displayOrder(1)
                    .isActive(true)
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .build();
            bannerRepository.saveAndFlush(migrated);
        }
    }

    private void syncPrimaryBannerToPageContent() {
        ServicesPageContent content = repository.findSingleton().orElseGet(this::createDefault);
        List<ServicesHeroBanner> active = bannerRepository.findAllByIsActiveTrueOrderByDisplayOrderAscCreatedAtAsc();
        if (!active.isEmpty()) {
            content.setHeroBannerImageUrl(active.get(0).getImageUrl());
            content.setHeroBannerImagePublicId(active.get(0).getImagePublicId());
        } else {
            content.setHeroBannerImageUrl(null);
            content.setHeroBannerImagePublicId(null);
        }
        content.setUpdatedAt(OffsetDateTime.now());
        repository.saveAndFlush(content);
    }

    private void deleteCloudinaryAssetIfUnreferenced(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        try {
            if (bannerRepository != null && bannerRepository.existsByImagePublicId(publicId)) {
                log.info("Asset with publicId {} is still referenced by another hero banner. Skipping Cloudinary deletion.", publicId);
                return;
            }
            if (portfolioItemRepository != null && portfolioItemRepository.existsByImagePublicId(publicId)) {
                log.info("Asset with publicId {} is still referenced by a portfolio item. Skipping Cloudinary deletion.", publicId);
                return;
            }
            if (mediaService != null) {
                mediaService.deleteImage(publicId);
            }
        } catch (Exception e) {
            log.warn("Failed to delete asset from Cloudinary (publicId: {}): {}", publicId, e.getMessage());
        }
    }

    private ServicesPageContent createDefault() {
        return ServicesPageContent.builder()
                .id((short) 1)
                .heroEyebrow("OUR SERVICES // FULL-SERVICE CREATIVE STUDIO")
                .heroHeadlineLine1("CREATIVE SOLUTIONS")
                .heroHeadlineLine2("THAT DRIVE")
                .heroHeadlineHighlight("RESULTS.")
                .heroDescription("From concept to final cut, we provide end-to-end creative services that help brands connect, engage, and grow. Every edit, thumbnail, and motion visual is crafted for maximum retention and measurable conversion.")
                .heroPrimaryCtaText("DISCUSS YOUR PROJECT")
                .heroPrimaryCtaLink("/contact")
                .heroSecondaryCtaText("VIEW WORK ARCHIVE")
                .heroSecondaryCtaLink("/portfolio")
                .heroVideoUrl("/hero-demo.mp4")
                .heroBannerImageUrl(null)
                .heroBannerImagePublicId(null)
                .heroStat1Value("4K+")
                .heroStat1Label("Master Exports")
                .heroStat2Value("48H")
                .heroStat2Label("Avg First Draft")
                .heroStat3Value("100%")
                .heroStat3Label("Retention Focus")
                .heroShowStats(true)
                .heroShowWorkstation(true)
                .servicesEyebrow("WHAT WE DO // PORTFOLIO DISCIPLINES")
                .servicesHeading("OUR SERVICES")
                .servicesDescription("High-quality creative services tailored to your brand’s unique needs. Engineered for digital retention and commercial performance.")
                .processEyebrow("HOW WE WORK // METHODOLOGY")
                .processHeading("OUR PROCESS")
                .processDescription("A structured five-step production workflow built for creative alignment, swift turnaround, and measurable results.")
                .toolsEyebrow("PRODUCTION STACK // HARDWARE & SOFTWARE")
                .toolsHeading("TOOLS WE USE")
                .toolsDescription("Industry-standard software suites power every frame, cut, grade, and render we produce for our clients.")
                .ctaEyebrow("STUDIO AVAILABILITY // NOW ACCEPTING COMMISSIONS")
                .ctaHeadingLine1("LET’S CREATE SOMETHING")
                .ctaHeadingLine2("AMAZING")
                .ctaHeadingHighlight("TOGETHER.")
                .ctaDescription("Have a project in mind? We're ready to bring your ideas to life with high-retention video editing, scroll-stopping thumbnails, and cinematic visual assets.")
                .ctaButtonText("GET IN TOUCH")
                .ctaButtonLink("/contact")
                .ctaAccentNote("● STUDIO AVAILABILITY // NOW ACCEPTING 2026 PROJECTS")
                .updatedAt(OffsetDateTime.now())
                .build();
    }
}
