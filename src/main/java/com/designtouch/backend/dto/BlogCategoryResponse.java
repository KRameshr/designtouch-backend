package com.designtouch.backend.dto;

import com.designtouch.backend.entity.BlogCategory;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BlogCategoryResponse(
        UUID id,
        String name,
        String slug,
        String description,
        Integer displayOrder,
        Long postCount,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static BlogCategoryResponse from(BlogCategory cat, Long postCount) {
        return new BlogCategoryResponse(
                cat.getId(),
                cat.getName(),
                cat.getSlug(),
                cat.getDescription(),
                cat.getDisplayOrder(),
                postCount != null ? postCount : 0L,
                cat.getCreatedAt(),
                cat.getUpdatedAt()
        );
    }
}

