package com.designtouch.backend.dto;

import com.designtouch.backend.entity.PortfolioPageContent;

import java.time.OffsetDateTime;

public record PortfolioPageContentResponse(
        Short id,
        String eyebrow,
        String headlineLine1,
        String headlineLine2,
        String headlineHighlight,
        String description,
        String introVideoUrl,
        String introVideoPublicId,
        OffsetDateTime updatedAt) {

    public static PortfolioPageContentResponse from(PortfolioPageContent entity) {
        if (entity == null) return null;
        return new PortfolioPageContentResponse(
                entity.getId(),
                entity.getEyebrow(),
                entity.getHeadlineLine1(),
                entity.getHeadlineLine2(),
                entity.getHeadlineHighlight(),
                entity.getDescription(),
                entity.getIntroVideoUrl(),
                entity.getIntroVideoPublicId(),
                entity.getUpdatedAt()
        );
    }
}

