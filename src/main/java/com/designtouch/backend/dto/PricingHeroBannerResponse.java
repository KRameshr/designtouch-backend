package com.designtouch.backend.dto;

import com.designtouch.backend.entity.PricingHeroBanner;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PricingHeroBannerResponse(
        UUID id,
        String label,
        String imageUrl,
        String imagePublicId,
        String altText,
        String objectPosition,
        Integer displayOrder,
        Boolean isActive,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static PricingHeroBannerResponse from(PricingHeroBanner b) {
        if (b == null) return null;
        return new PricingHeroBannerResponse(
                b.getId(),
                b.getLabel(),
                b.getImageUrl(),
                b.getImagePublicId(),
                b.getAltText() != null ? b.getAltText() : "DesignTouch+ Creative Production Studio",
                b.getObjectPosition() != null ? b.getObjectPosition() : "center",
                b.getDisplayOrder(),
                b.getIsActive() != null ? b.getIsActive() : true,
                b.getCreatedAt(),
                b.getUpdatedAt()
        );
    }
}

