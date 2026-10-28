package com.designtouch.backend.dto;

public record ServicesHeroBannerRequest(
        String label,
        String imageUrl,
        String imagePublicId,
        Integer displayOrder,
        Boolean isActive
) {
}

