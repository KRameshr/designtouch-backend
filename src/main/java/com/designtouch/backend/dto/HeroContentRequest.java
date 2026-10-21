package com.designtouch.backend.dto;

import jakarta.validation.constraints.Size;

public record HeroContentRequest(
        @Size(max = 150) String eyebrow,
        @Size(max = 150) String headlineLine1,
        @Size(max = 150) String headlineLine2,
        @Size(max = 150) String headlineLine3,
        @Size(max = 150) String headlineHighlight,
        String subheading,
        @Size(max = 100) String primaryCtaText,
        @Size(max = 200) String primaryCtaLink,
        @Size(max = 100) String secondaryCtaText,
        @Size(max = 200) String secondaryCtaLink,
        String videoUrl,
        @Size(max = 100) String badgeText,
        @Size(max = 100) String specBadgeText
) {
}

