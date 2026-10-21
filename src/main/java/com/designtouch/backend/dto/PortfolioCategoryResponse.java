package com.designtouch.backend.dto;

import com.designtouch.backend.entity.PortfolioCategory;
import com.designtouch.backend.entity.enums.CategoryType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PortfolioCategoryResponse(
        UUID id,
        String name,
        String slug,
        CategoryType categoryType,
        Integer displayOrder,
        Boolean isActive,
        Long projectCount,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static PortfolioCategoryResponse from(PortfolioCategory cat, Long projectCount) {
        return new PortfolioCategoryResponse(
                cat.getId(),
                cat.getName(),
                cat.getSlug(),
                cat.getCategoryType(),
                cat.getDisplayOrder(),
                cat.getIsActive(),
                projectCount != null ? projectCount : 0L,
                cat.getCreatedAt(),
                cat.getUpdatedAt()
        );
    }

    public static PortfolioCategoryResponse from(PortfolioCategory cat) {
        return from(cat, 0L);
    }
}

