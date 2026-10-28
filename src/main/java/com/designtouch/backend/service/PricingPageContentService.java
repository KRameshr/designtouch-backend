package com.designtouch.backend.service;

import com.designtouch.backend.dto.PricingHeroBannerRequest;
import com.designtouch.backend.dto.PricingHeroBannerResponse;
import com.designtouch.backend.dto.PricingPageContentRequest;
import com.designtouch.backend.dto.PricingPageContentResponse;
import com.designtouch.backend.entity.PricingHeroBanner;
import com.designtouch.backend.entity.PricingPageContent;
import com.designtouch.backend.repository.PricingHeroBannerRepository;
import com.designtouch.backend.repository.PricingPageContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingPageContentService {

    public static final int MAX_HERO_BANNERS = 5;

    private final PricingPageContentRepository repository;
    private final PricingHeroBannerRepository bannerRepository;

    @Transactional(readOnly = true)
    public PricingPageContentResponse getPricingPageContent() {
        PricingPageContent content = getOrCreateSingleton();
        List<PricingHeroBannerResponse> activeBanners = getActiveHeroBanners();
        return PricingPageContentResponse.from(content, activeBanners);
    }

    @Transactional(readOnly = true)
    public List<PricingHeroBannerResponse> getActiveHeroBanners() {
        PricingPageContent content = repository.findById((short) 1).orElse(null);
        if (content != null) {
            ensureLegacyBannerMigrated(content);
        }
        return bannerRepository.findAllByIsActiveTrueOrderByDisplayOrderAscCreatedAtAsc()
                .stream()
                .map(PricingHeroBannerResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PricingHeroBannerResponse> getAllHeroBannersAdmin() {
        PricingPageContent content = repository.findById((short) 1).orElse(null);
        if (content != null) {
            ensureLegacyBannerMigrated(content);
        }
        return bannerRepository.findAllByOrderByDisplayOrderAscCreatedAtAsc()
                .stream()
                .map(PricingHeroBannerResponse::from)
                .toList();
    }

    @Transactional
    public PricingHeroBannerResponse createHeroBanner(PricingHeroBannerRequest request) {
        PricingPageContent content = getOrCreateSingleton();
        ensureLegacyBannerMigrated(content);

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

        String altText = (request.altText() != null && !request.altText().isBlank())
                ? request.altText().trim()
                : "DesignTouch+ Creative Production Studio";

        String objectPosition = (request.objectPosition() != null && !request.objectPosition().isBlank())
                ? request.objectPosition().trim()
                : "center";

        PricingHeroBanner banner = PricingHeroBanner.builder()
                .label(label)
                .imageUrl(request.imageUrl().trim())
                .imagePublicId(request.imagePublicId() != null && !request.imagePublicId().isBlank() ? request.imagePublicId().trim() : null)
                .altText(altText)
                .objectPosition(objectPosition)
                .displayOrder(nextOrder)
                .isActive(request.isActive() != null ? request.isActive() : true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        PricingHeroBanner saved = bannerRepository.saveAndFlush(banner);
        syncPrimaryBannerToPageContent();
        return PricingHeroBannerResponse.from(saved);
    }

    @Transactional
    public PricingHeroBannerResponse updateHeroBanner(UUID id, PricingHeroBannerRequest request) {
        PricingHeroBanner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pricing hero banner not found: " + id));

        if (request.imageUrl() != null && !request.imageUrl().isBlank()) {
            banner.setImageUrl(request.imageUrl().trim());
        }
        if (request.label() != null) {
            banner.setLabel(request.label().trim());
        }
        if (request.imagePublicId() != null) {
            banner.setImagePublicId(request.imagePublicId().trim());
        }
        if (request.altText() != null) {
            banner.setAltText(request.altText().trim());
        }
        if (request.objectPosition() != null) {
            banner.setObjectPosition(request.objectPosition().trim());
        }
        if (request.displayOrder() != null) {
            banner.setDisplayOrder(request.displayOrder());
        }
        if (request.isActive() != null) {
            banner.setIsActive(request.isActive());
        }
        banner.setUpdatedAt(OffsetDateTime.now());

        PricingHeroBanner saved = bannerRepository.saveAndFlush(banner);
        syncPrimaryBannerToPageContent();
        return PricingHeroBannerResponse.from(saved);
    }

    @Transactional
    public PricingHeroBannerResponse toggleHeroBannerActive(UUID id) {
        PricingHeroBanner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pricing hero banner not found: " + id));

        boolean currentStatus = banner.getIsActive() != null ? banner.getIsActive() : true;
        banner.setIsActive(!currentStatus);
        banner.setUpdatedAt(OffsetDateTime.now());

        PricingHeroBanner saved = bannerRepository.saveAndFlush(banner);
        syncPrimaryBannerToPageContent();
        return PricingHeroBannerResponse.from(saved);
    }

    @Transactional
    public List<PricingHeroBannerResponse> reorderHeroBanners(List<UUID> orderedIds) {
        if (orderedIds == null || orderedIds.isEmpty()) {
            return getAllHeroBannersAdmin();
        }

        List<PricingHeroBanner> all = bannerRepository.findAllByOrderByDisplayOrderAscCreatedAtAsc();
        for (int i = 0; i < orderedIds.size(); i++) {
            UUID targetId = orderedIds.get(i);
            for (PricingHeroBanner b : all) {
                if (b.getId().equals(targetId)) {
                    b.setDisplayOrder(i + 1);
                    b.setUpdatedAt(OffsetDateTime.now());
                    bannerRepository.save(b);
                    break;
                }
            }
        }
        bannerRepository.flush();
        syncPrimaryBannerToPageContent();
        return getAllHeroBannersAdmin();
    }

    @Transactional
    public void deleteHeroBanner(UUID id) {
        PricingHeroBanner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pricing hero banner not found: " + id));

        bannerRepository.delete(banner);
        bannerRepository.flush();

        // Re-compact display orders
        List<PricingHeroBanner> remaining = bannerRepository.findAllByOrderByDisplayOrderAscCreatedAtAsc();
        for (int i = 0; i < remaining.size(); i++) {
            PricingHeroBanner b = remaining.get(i);
            b.setDisplayOrder(i + 1);
            bannerRepository.save(b);
        }
        bannerRepository.flush();
        syncPrimaryBannerToPageContent();
    }

    @Transactional
    public PricingPageContentResponse updatePricingPageContent(PricingPageContentRequest request) {
        PricingPageContent content = getOrCreateSingleton();

        if (request.heroEyebrow() != null) content.setHeroEyebrow(request.heroEyebrow());
        if (request.heroHeadlineLine1() != null) content.setHeroHeadlineLine1(request.heroHeadlineLine1());
        if (request.heroHeadlineLine2() != null) content.setHeroHeadlineLine2(request.heroHeadlineLine2());
        if (request.heroHeadlineHighlight() != null) content.setHeroHeadlineHighlight(request.heroHeadlineHighlight());
        if (request.heroHighlightStyle() != null) content.setHeroHighlightStyle(request.heroHighlightStyle());
        if (request.heroDescription() != null) content.setHeroDescription(request.heroDescription());
        if (request.heroBannerImageUrl() != null) content.setHeroBannerImageUrl(request.heroBannerImageUrl());
        if (request.heroBannerImagePublicId() != null) content.setHeroBannerImagePublicId(request.heroBannerImagePublicId());
        if (request.heroBannerImageAlt() != null) content.setHeroBannerImageAlt(request.heroBannerImageAlt());
        if (request.heroBannerObjectPosition() != null) content.setHeroBannerObjectPosition(request.heroBannerObjectPosition());
        if (request.heroPrimaryCtaText() != null) content.setHeroPrimaryCtaText(request.heroPrimaryCtaText());
        if (request.heroPrimaryCtaLink() != null) content.setHeroPrimaryCtaLink(request.heroPrimaryCtaLink());
        if (request.heroSecondaryCtaText() != null) content.setHeroSecondaryCtaText(request.heroSecondaryCtaText());
        if (request.heroSecondaryCtaLink() != null) content.setHeroSecondaryCtaLink(request.heroSecondaryCtaLink());
        if (request.heroBackgroundImageUrl() != null) content.setHeroBackgroundImageUrl(request.heroBackgroundImageUrl());
        content.setUpdatedAt(OffsetDateTime.now());

        PricingPageContent saved = repository.save(content);
        List<PricingHeroBannerResponse> activeBanners = getActiveHeroBanners();
        return PricingPageContentResponse.from(saved, activeBanners);
    }

    @Transactional
    public PricingPageContentResponse deleteHeroBannerImage() {
        PricingPageContent content = getOrCreateSingleton();
        content.setHeroBannerImageUrl(null);
        content.setHeroBannerImagePublicId(null);
        content.setHeroBackgroundImageUrl(null);
        content.setUpdatedAt(OffsetDateTime.now());
        PricingPageContent saved = repository.save(content);
        List<PricingHeroBannerResponse> activeBanners = getActiveHeroBanners();
        return PricingPageContentResponse.from(saved, activeBanners);
    }

    private PricingPageContent getOrCreateSingleton() {
        return repository.findById((short) 1)
                .orElseGet(() -> repository.save(PricingPageContent.builder()
                        .id((short) 1)
                        .heroEyebrow("PRICING PLANS")
                        .heroHeadlineLine1("SIMPLE PRICING.")
                        .heroHeadlineLine2("POWERFUL")
                        .heroHeadlineHighlight("RESULTS.")
                        .heroHighlightStyle("gradient")
                        .heroDescription("Choose the perfect plan for your needs. All plans include high-quality editing, fast delivery and unlimited revisions.")
                        .heroBannerImageAlt("DesignTouch+ Creative Production Studio")
                        .heroBannerObjectPosition("center")
                        .heroPrimaryCtaText("CHOOSE A PLAN")
                        .heroPrimaryCtaLink("#plans")
                        .heroSecondaryCtaText("BOOK A CALL")
                        .heroSecondaryCtaLink("/contact")
                        .build()));
    }

    private void ensureLegacyBannerMigrated(PricingPageContent content) {
        String legacyUrl = content.getHeroBannerImageUrl() != null && !content.getHeroBannerImageUrl().isBlank()
                ? content.getHeroBannerImageUrl().trim()
                : (content.getHeroBackgroundImageUrl() != null && !content.getHeroBackgroundImageUrl().isBlank()
                ? content.getHeroBackgroundImageUrl().trim()
                : null);

        if (legacyUrl != null && !legacyUrl.isBlank() && bannerRepository.count() == 0) {
            log.info("Migrating legacy single hero banner into pricing_hero_banner table: {}", legacyUrl);
            PricingHeroBanner first = PricingHeroBanner.builder()
                    .label("Banner 1")
                    .imageUrl(legacyUrl)
                    .imagePublicId(content.getHeroBannerImagePublicId())
                    .altText(content.getHeroBannerImageAlt() != null ? content.getHeroBannerImageAlt() : "DesignTouch+ Creative Production Studio")
                    .objectPosition(content.getHeroBannerObjectPosition() != null ? content.getHeroBannerObjectPosition() : "center")
                    .displayOrder(1)
                    .isActive(true)
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .build();
            bannerRepository.saveAndFlush(first);
        }
    }

    private void syncPrimaryBannerToPageContent() {
        PricingPageContent content = getOrCreateSingleton();
        List<PricingHeroBanner> active = bannerRepository.findAllByIsActiveTrueOrderByDisplayOrderAscCreatedAtAsc();
        if (!active.isEmpty()) {
            PricingHeroBanner primary = active.get(0);
            content.setHeroBannerImageUrl(primary.getImageUrl());
            content.setHeroBannerImagePublicId(primary.getImagePublicId());
            content.setHeroBannerImageAlt(primary.getAltText());
            content.setHeroBannerObjectPosition(primary.getObjectPosition());
            content.setHeroBackgroundImageUrl(primary.getImageUrl());
        } else {
            content.setHeroBannerImageUrl(null);
            content.setHeroBannerImagePublicId(null);
            content.setHeroBackgroundImageUrl(null);
        }
        content.setUpdatedAt(OffsetDateTime.now());
        repository.save(content);
    }
}

