package com.designtouch.backend.dto;

import com.designtouch.backend.entity.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PortfolioCategoryRequest(
        @NotBlank(message = "Category name is required")
        @Size(max = 100, message = "Category name must not exceed 100 characters")
        String name,

        String slug,

        @NotNull(message = "Category type is required")
        CategoryType categoryType,

        Integer displayOrder,

        Boolean isActive
) {
}

