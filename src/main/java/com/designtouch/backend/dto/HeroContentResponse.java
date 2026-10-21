package com.designtouch.backend.dto;

import com.designtouch.backend.entity.HeroContent;

import java.time.OffsetDateTime;

public record HeroContentResponse(
        String eyebrow,
        String headlineLine1,
        String headlineLine2,
        String headlineLine3,
        String headlineHighlight,
        String subheading,
        String primaryCtaText,
        String primaryCtaLink,
        String secondaryCtaText,
        String secondaryCtaLink,
        String videoUrl,
        String badgeText,
        String specBadgeText,
        OffsetDateTime updatedAt
) {
    public static HeroContentResponse from(HeroContent h) {
        return new HeroContentResponse(
                h.getEyebrow(),
                h.getHeadlineLine1(),
                h.getHeadlineLine2(),
                h.getHeadlineLine3(),
                h.getHeadlineHighlight(),
                h.getSubheading(),
                h.getPrimaryCtaText(),
                h.getPrimaryCtaLink(),
                h.getSecondaryCtaText(),
                h.getSecondaryCtaLink(),
                h.getVideoUrl(),
                h.getBadgeText(),
                h.getSpecBadgeText(),
                h.getUpdatedAt()
        );
    }
}

