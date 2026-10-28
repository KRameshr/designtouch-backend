package com.designtouch.backend.dto;

import com.designtouch.backend.entity.BlogPageContent;

public record BlogPageContentResponse(
        Short id,
        String heroEyebrow,
        String heroHeadlineLine1,
        String heroHeadlineLine2,
        String heroHeadlineHighlight,
        String heroDescription,
        String heroCtaText,
        String heroCtaLink,
        String heroBannerImageUrl,
        String heroBannerImagePublicId,
        String heroBannerImageAlt,
        String heroBannerObjectPosition
) {
    public static BlogPageContentResponse from(BlogPageContent c) {
        if (c == null) {
            return fallback();
        }
        return new BlogPageContentResponse(
                c.getId(),
                c.getHeroEyebrow() != null ? c.getHeroEyebrow() : "OUR BLOG",
                c.getHeroHeadlineLine1() != null ? c.getHeroHeadlineLine1() : "INSIGHTS THAT",
                c.getHeroHeadlineLine2() != null ? c.getHeroHeadlineLine2() : "INSPIRE",
                c.getHeroHeadlineHighlight() != null ? c.getHeroHeadlineHighlight() : "CREATIVITY.",
                c.getHeroDescription() != null ? c.getHeroDescription() : "Tips, trends and tutorials on video editing, motion graphics, content creation and growing your brand.",
                c.getHeroCtaText() != null ? c.getHeroCtaText() : "EXPLORE ALL ARTICLES",
                c.getHeroCtaLink() != null ? c.getHeroCtaLink() : "#articles",
                c.getHeroBannerImageUrl() != null ? c.getHeroBannerImageUrl() : "https://res.cloudinary.com/dggikfrj5/image/upload/v1792669852/designtouch/services/cygu80p1hmhkqgvpc3au.png",
                c.getHeroBannerImagePublicId(),
                c.getHeroBannerImageAlt() != null ? c.getHeroBannerImageAlt() : "DesignTouch+ Creative Workspace Studio",
                c.getHeroBannerObjectPosition() != null ? c.getHeroBannerObjectPosition() : "center right"
        );
    }

    public static BlogPageContentResponse fallback() {
        return new BlogPageContentResponse(
                (short) 1,
                "OUR BLOG",
                "INSIGHTS THAT",
                "INSPIRE",
                "CREATIVITY.",
                "Tips, trends and tutorials on video editing, motion graphics, content creation and growing your brand.",
                "EXPLORE ALL ARTICLES",
                "#articles",
                "https://res.cloudinary.com/dggikfrj5/image/upload/v1792669852/designtouch/services/cygu80p1hmhkqgvpc3au.png",
                null,
                "DesignTouch+ Creative Workspace Studio",
                "center right"
        );
    }
}
