package com.designtouch.backend.dto;

import com.designtouch.backend.entity.PricingPageContent;

import java.util.List;

public record PricingPageContentResponse(
        Short id,
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
        String heroBackgroundImageUrl,
        List<PricingHeroBannerResponse> heroBanners
) {
    public static PricingPageContentResponse from(PricingPageContent c, List<PricingHeroBannerResponse> banners) {
        if (c == null) return null;
        String bannerUrl = c.getHeroBannerImageUrl();
        if ((bannerUrl == null || bannerUrl.isBlank()) && c.getHeroBackgroundImageUrl() != null && !c.getHeroBackgroundImageUrl().isBlank()) {
            bannerUrl = c.getHeroBackgroundImageUrl();
        }
        if ((bannerUrl == null || bannerUrl.isBlank()) && banners != null && !banners.isEmpty()) {
            bannerUrl = banners.get(0).imageUrl();
        }
        return new PricingPageContentResponse(
                c.getId(),
                c.getHeroEyebrow(),
                c.getHeroHeadlineLine1(),
                c.getHeroHeadlineLine2(),
                c.getHeroHeadlineHighlight(),
                c.getHeroHighlightStyle() != null ? c.getHeroHighlightStyle() : "gradient",
                c.getHeroDescription(),
                bannerUrl,
                c.getHeroBannerImagePublicId(),
                c.getHeroBannerImageAlt() != null ? c.getHeroBannerImageAlt() : "DesignTouch+ Creative Production Studio",
                c.getHeroBannerObjectPosition() != null ? c.getHeroBannerObjectPosition() : "center",
                c.getHeroPrimaryCtaText() != null ? c.getHeroPrimaryCtaText() : "CHOOSE A PLAN",
                c.getHeroPrimaryCtaLink() != null ? c.getHeroPrimaryCtaLink() : "#plans",
                c.getHeroSecondaryCtaText() != null ? c.getHeroSecondaryCtaText() : "BOOK A CALL",
                c.getHeroSecondaryCtaLink() != null ? c.getHeroSecondaryCtaLink() : "/contact",
                c.getHeroBackgroundImageUrl(),
                banners != null ? banners : List.of()
        );
    }

    public static PricingPageContentResponse from(PricingPageContent c) {
        return from(c, List.of());
    }
}
