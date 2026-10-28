package com.designtouch.backend.dto;

import com.designtouch.backend.entity.enums.BlogPostStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record BlogPostRequest(
        @NotBlank @Size(max = 250, message = "Article title cannot exceed 250 characters") String title,
        @NotBlank @Size(max = 250, message = "Article slug cannot exceed 250 characters") String slug,
        @Size(max = 500, message = "Article excerpt cannot exceed 500 characters") String excerpt,
        @NotBlank String content,
        String coverImageUrl,
        String coverImagePublicId,
        String category,
        UUID categoryId,
        Integer readingTime,
        BlogPostStatus status,
        OffsetDateTime scheduledFor,
        OffsetDateTime publishedAt,
        Boolean isPublished,
        @Size(max = 300, message = "SEO title cannot exceed 300 characters") String seoTitle,
        String metaDescription,
        List<String> tags,
        List<String> focusKeywords) {
}
