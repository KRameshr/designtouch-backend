package com.designtouch.backend.dto;

public record ServicesPageContentRequest(
        // Hero
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

        // Headers
        String servicesEyebrow,
        String servicesHeading,
        String servicesDescription,

        String processEyebrow,
        String processHeading,
        String processDescription,

        String toolsEyebrow,
        String toolsHeading,
        String toolsDescription,

        // CTA
        String ctaEyebrow,
        String ctaHeadingLine1,
        String ctaHeadingLine2,
        String ctaHeadingHighlight,
        String ctaDescription,
        String ctaButtonText,
        String ctaButtonLink,
        String ctaAccentNote
) {
    // Backward-compatible constructor for callers not passing banner image fields
    public ServicesPageContentRequest(
            String heroEyebrow, String heroHeadlineLine1, String heroHeadlineLine2, String heroHeadlineHighlight,
            String heroDescription, String heroPrimaryCtaText, String heroPrimaryCtaLink, String heroSecondaryCtaText,
            String heroSecondaryCtaLink, String heroVideoUrl, String heroStat1Value, String heroStat1Label,
            String heroStat2Value, String heroStat2Label, String heroStat3Value, String heroStat3Label,
            Boolean heroShowStats, Boolean heroShowWorkstation,
            String servicesEyebrow, String servicesHeading, String servicesDescription,
            String processEyebrow, String processHeading, String processDescription,
            String toolsEyebrow, String toolsHeading, String toolsDescription,
            String ctaEyebrow, String ctaHeadingLine1, String ctaHeadingLine2, String ctaHeadingHighlight,
            String ctaDescription, String ctaButtonText, String ctaButtonLink, String ctaAccentNote
    ) {
        this(heroEyebrow, heroHeadlineLine1, heroHeadlineLine2, heroHeadlineHighlight,
             heroDescription, heroPrimaryCtaText, heroPrimaryCtaLink, heroSecondaryCtaText,
             heroSecondaryCtaLink, heroVideoUrl, heroStat1Value, heroStat1Label,
             heroStat2Value, heroStat2Label, heroStat3Value, heroStat3Label,
             heroShowStats, heroShowWorkstation, null, null,
             servicesEyebrow, servicesHeading, servicesDescription,
             processEyebrow, processHeading, processDescription,
             toolsEyebrow, toolsHeading, toolsDescription,
             ctaEyebrow, ctaHeadingLine1, ctaHeadingLine2, ctaHeadingHighlight,
             ctaDescription, ctaButtonText, ctaButtonLink, ctaAccentNote);
    }
}

