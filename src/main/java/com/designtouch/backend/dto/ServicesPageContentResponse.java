package com.designtouch.backend.dto;

import com.designtouch.backend.entity.ServicesPageContent;

import java.time.OffsetDateTime;

public record ServicesPageContentResponse(
        Short id,
        String heroEyebrow,
        String heroHeadlineLine1,
        String heroHeadlineLine2,
        String heroHeadlineHighlight,
        String heroDescription,
        String heroPrimaryCtaText,
        String heroPrimaryCtaLink,
        String heroSecondaryCtaText,
        String heroSecondaryCtaLink,
        String heroVideoUrl,
        String heroStat1Value,
        String heroStat1Label,
        String heroStat2Value,
        String heroStat2Label,
        String heroStat3Value,
        String heroStat3Label,
        Boolean heroShowStats,
        Boolean heroShowWorkstation,
        String heroBannerImageUrl,
        String heroBannerImagePublicId,

        String servicesEyebrow,
        String servicesHeading,
        String servicesDescription,

        String processEyebrow,
        String processHeading,
        String processDescription,

        String toolsEyebrow,
        String toolsHeading,
        String toolsDescription,

        String ctaEyebrow,
        String ctaHeadingLine1,
        String ctaHeadingLine2,
        String ctaHeadingHighlight,
        String ctaDescription,
        String ctaButtonText,
        String ctaButtonLink,
        String ctaAccentNote,
        OffsetDateTime updatedAt
) {
    public static ServicesPageContentResponse from(ServicesPageContent c) {
        return new ServicesPageContentResponse(
                c.getId(),
                c.getHeroEyebrow(),
                c.getHeroHeadlineLine1(),
                c.getHeroHeadlineLine2(),
                c.getHeroHeadlineHighlight(),
                c.getHeroDescription(),
                c.getHeroPrimaryCtaText(),
                c.getHeroPrimaryCtaLink(),
                c.getHeroSecondaryCtaText(),
                c.getHeroSecondaryCtaLink(),
                c.getHeroVideoUrl(),
                c.getHeroStat1Value(),
                c.getHeroStat1Label(),
                c.getHeroStat2Value(),
                c.getHeroStat2Label(),
                c.getHeroStat3Value(),
                c.getHeroStat3Label(),
                c.getHeroShowStats() != null ? c.getHeroShowStats() : true,
                c.getHeroShowWorkstation() != null ? c.getHeroShowWorkstation() : true,
                c.getHeroBannerImageUrl(),
                c.getHeroBannerImagePublicId(),

                c.getServicesEyebrow(),
                c.getServicesHeading(),
                c.getServicesDescription(),

                c.getProcessEyebrow(),
                c.getProcessHeading(),
                c.getProcessDescription(),

                c.getToolsEyebrow(),
                c.getToolsHeading(),
                c.getToolsDescription(),

                c.getCtaEyebrow(),
                c.getCtaHeadingLine1(),
                c.getCtaHeadingLine2(),
                c.getCtaHeadingHighlight(),
                c.getCtaDescription(),
                c.getCtaButtonText(),
                c.getCtaButtonLink(),
                c.getCtaAccentNote(),
                c.getUpdatedAt()
        );
    }
}

