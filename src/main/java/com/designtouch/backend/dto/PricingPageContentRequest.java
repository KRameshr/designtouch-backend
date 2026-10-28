package com.designtouch.backend.dto;

public record PricingPageContentRequest(
        String heroEyebrow,
        String heroHeadlineLine1,
        String heroHeadlineLine2,
        String heroHeadlineHighlight,
        String heroHighlightStyle,
        String heroDescription,
        String heroBannerImageUrl,
        String heroBannerImagePublicId,
        String heroBannerImageAlt,
        String heroBannerObjectPosition,
        String heroPrimaryCtaText,
        String heroPrimaryCtaLink,
        String heroSecondaryCtaText,
        String heroSecondaryCtaLink,
        String heroBackgroundImageUrl
) {
}
