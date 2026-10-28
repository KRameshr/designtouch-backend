package com.designtouch.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BlogCategoryRequest(
        @NotBlank(message = "Category name is required")
        @Size(max = 100, message = "Category name cannot exceed 100 characters")
        String name,

        @Size(max = 100, message = "Category slug cannot exceed 100 characters")
        String slug,

        String description,

        Integer displayOrder
) {
}

