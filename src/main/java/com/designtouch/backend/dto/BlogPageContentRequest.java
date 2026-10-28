package com.designtouch.backend.dto;

import jakarta.validation.constraints.Size;

public record BlogPageContentRequest(
        @Size(max = 150, message = "Hero eyebrow must not exceed 150 characters")
        String heroEyebrow,

        @Size(max = 150, message = "Headline line 1 must not exceed 150 characters")
        String heroHeadlineLine1,

        @Size(max = 150, message = "Headline line 2 must not exceed 150 characters")
        String heroHeadlineLine2,

        @Size(max = 150, message = "Headline highlight must not exceed 150 characters")
        String heroHeadlineHighlight,

        String heroDescription,

        @Size(max = 100, message = "CTA text must not exceed 100 characters")
        String heroCtaText,

        @Size(max = 200, message = "CTA link must not exceed 200 characters")
        String heroCtaLink,

        String heroBannerImageUrl,
        String heroBannerImagePublicId,

        @Size(max = 255, message = "Alt text must not exceed 255 characters")
        String heroBannerImageAlt,

        @Size(max = 50, message = "Object position must not exceed 50 characters")
        String heroBannerObjectPosition
) {
}
