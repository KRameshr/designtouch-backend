package com.designtouch.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record PricingHeroBannerRequest(
        String label,
        @NotBlank(message = "Banner image URL is required")
        String imageUrl,
        String imagePublicId,
        String altText,
        String objectPosition,
        Integer displayOrder,
        Boolean isActive
) {
}

