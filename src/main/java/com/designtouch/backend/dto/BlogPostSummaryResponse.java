package com.designtouch.backend.dto;

import com.designtouch.backend.entity.BlogPost;
import com.designtouch.backend.entity.enums.BlogCategory;

import java.time.OffsetDateTime;
import java.util.UUID;

// Lightweight version for the blog grid / popular posts sidebar — no full content body
public record BlogPostSummaryResponse(
        UUID id, String title, String slug, String excerpt, String coverImageUrl,
        BlogCategory category, Integer readingTime, OffsetDateTime publishedAt) {

    public static BlogPostSummaryResponse from(BlogPost b) {
        return new BlogPostSummaryResponse(
                b.getId(), b.getTitle(), b.getSlug(), b.getExcerpt(), b.getCoverImageUrl(),
                b.getCategory(), b.getReadingTime(), b.getPublishedAt());
    }
}
