package com.designtouch.backend.dto;

import com.designtouch.backend.entity.enums.BlogCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BlogPostRequest(
        @NotBlank String title,
        @NotBlank String slug,
        String excerpt,
        @NotBlank String content,
        String coverImageUrl,
        @NotNull BlogCategory category,
        Integer readingTime,
        Boolean isPublished) {
}
