package com.designtouch.backend.dto;

import com.designtouch.backend.entity.enums.ContentType;
import com.designtouch.backend.entity.enums.PortfolioCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Request body for admin create/update — no id/createdAt/viewCount (server-controlled)
public record PortfolioItemRequest(
        @NotBlank String title,
        @NotNull ContentType contentType,
        PortfolioCategory category,
        String subCategory,
        String description,
        String duration,
        String videoUrl,
        String imageUrl,
        String thumbnailUrl,
        Integer displayOrder,
        Boolean isFeatured) {
}
