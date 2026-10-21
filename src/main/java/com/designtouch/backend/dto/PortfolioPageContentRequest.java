package com.designtouch.backend.dto;

public record PortfolioPageContentRequest(
        String eyebrow,
        String headlineLine1,
        String headlineLine2,
        String headlineHighlight,
        String description,
        String introVideoUrl,
        String introVideoPublicId) {
}

