package com.designtouch.backend.service;

import com.designtouch.backend.dto.ServicesPageContentRequest;
import com.designtouch.backend.dto.ServicesPageContentResponse;
import com.designtouch.backend.entity.ServicesPageContent;
import com.designtouch.backend.repository.PortfolioItemRepository;
import com.designtouch.backend.repository.ServicesPageContentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class ServicesPageContentService {

    private static final Logger log = LoggerFactory.getLogger(ServicesPageContentService.class);

    private final ServicesPageContentRepository repository;
    private final MediaService mediaService;
    private final PortfolioItemRepository portfolioItemRepository;

    @Transactional(readOnly = true)
    public ServicesPageContentResponse getServicesPageContent() {
        ServicesPageContent content = repository.findSingleton()
                .orElseGet(this::createDefault);
        return ServicesPageContentResponse.from(content);
    }

    @Transactional
    public ServicesPageContentResponse updateServicesPageContent(ServicesPageContentRequest request) {
        ServicesPageContent content = repository.findSingleton()
                .orElseGet(this::createDefault);

        // If banner image was replaced or cleared, safely clean up the old Cloudinary asset
        String oldPublicId = content.getHeroBannerImagePublicId();
        String newPublicId = request.heroBannerImagePublicId();
        if (oldPublicId != null && !oldPublicId.isBlank() && !oldPublicId.equals(newPublicId)) {
            deleteCloudinaryAssetIfUnreferenced(oldPublicId);
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
        content.setHeroBannerImageUrl(request.heroBannerImageUrl());
        content.setHeroBannerImagePublicId(request.heroBannerImagePublicId());

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

        return ServicesPageContentResponse.from(repository.save(content));
    }

    @Transactional
    public ServicesPageContentResponse deleteHeroBannerImage() {
        ServicesPageContent content = repository.findSingleton()
                .orElseGet(this::createDefault);

        String oldPublicId = content.getHeroBannerImagePublicId();
        content.setHeroBannerImageUrl(null);
        content.setHeroBannerImagePublicId(null);
        content.setUpdatedAt(OffsetDateTime.now());
        ServicesPageContent saved = repository.save(content);

        if (oldPublicId != null && !oldPublicId.isBlank()) {
            deleteCloudinaryAssetIfUnreferenced(oldPublicId);
        }

        return ServicesPageContentResponse.from(saved);
    }

    private void deleteCloudinaryAssetIfUnreferenced(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        try {
            if (portfolioItemRepository != null && portfolioItemRepository.existsByImagePublicId(publicId)) {
                log.info("Asset with publicId {} is still referenced elsewhere. Skipping Cloudinary deletion.", publicId);
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

