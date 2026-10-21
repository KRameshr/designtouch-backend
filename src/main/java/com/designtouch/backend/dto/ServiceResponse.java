package com.designtouch.backend.dto;

import com.designtouch.backend.entity.Service;

import java.util.List;
import java.util.UUID;

public record ServiceResponse(
        UUID id,
        String icon,
        String title,
        String description,
        String shortDescription,
        String fullDescription,
        List<String> features,
        String colorTheme,
        String slug,
        String orderNumber,
        Integer displayOrder,
        String ctaLabel,
        String ctaUrl,
        String imageUrl,
        Boolean isActive) {

    public static ServiceResponse from(Service s) {
        String shortDesc = s.getShortDescription() != null && !s.getShortDescription().isBlank()
                ? s.getShortDescription()
                : s.getDescription();

        return new ServiceResponse(
                s.getId(),
                s.getIcon(),
                s.getTitle(),
                s.getDescription(),
                shortDesc,
                s.getFullDescription(),
                s.getFeatures(),
                s.getColorTheme(),
                s.getSlug(),
                s.getOrderNumber(),
                s.getDisplayOrder(),
                s.getCtaLabel(),
                s.getCtaUrl(),
                s.getImageUrl(),
                s.getIsActive() != null ? s.getIsActive() : true
        );
    }
}
