package com.designtouch.backend.dto;

import com.designtouch.backend.entity.ServicesHeroBanner;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ServicesHeroBannerResponse(
        UUID id,
        String label,
        String imageUrl,
        String imagePublicId,
        Integer displayOrder,
        Boolean isActive,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ServicesHeroBannerResponse from(ServicesHeroBanner b) {
        return new ServicesHeroBannerResponse(
                b.getId(),
                b.getLabel(),
                b.getImageUrl(),
                b.getImagePublicId(),
                b.getDisplayOrder(),
                b.getIsActive() != null ? b.getIsActive() : true,
                b.getCreatedAt(),
                b.getUpdatedAt()
        );
    }
}

