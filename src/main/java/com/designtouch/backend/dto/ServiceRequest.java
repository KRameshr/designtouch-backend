package com.designtouch.backend.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ServiceRequest(
        String icon,
        @NotBlank String title,
        String description,
        String shortDescription,
        String fullDescription,
        List<String> features,
        String colorTheme,
        @NotBlank String slug,
        String orderNumber,
        Integer displayOrder,
        String ctaLabel,
        String ctaUrl,
        String imageUrl,
        Boolean isActive) {
}
