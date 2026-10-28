package com.designtouch.backend.dto;

import com.designtouch.backend.entity.BlogPost;
import com.designtouch.backend.entity.enums.BlogPostStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

// Lightweight version for the blog grid / popular posts sidebar / admin table — no full content body
public record BlogPostSummaryResponse(
        UUID id,
        String title,
        String slug,
        String excerpt,
        String coverImageUrl,
        String coverImagePublicId,
        String category,
        UUID categoryId,
        String categoryName,
        Integer readingTime,
        BlogPostStatus status,
        Boolean isPublished,
        OffsetDateTime scheduledFor,
        OffsetDateTime publishedAt,
        String seoTitle,
        String metaDescription,
        List<String> tags,
        List<String> focusKeywords) {

    public static BlogPostSummaryResponse from(BlogPost b) {
        UUID categoryId = b.getCategoryRef() != null ? b.getCategoryRef().getId() : null;
        String categoryName = b.getCategoryRef() != null ? b.getCategoryRef().getName() : b.getCategory();
        String categorySlug = b.getCategoryRef() != null ? b.getCategoryRef().getSlug() : b.getCategory();

        return new BlogPostSummaryResponse(
                b.getId(),
                b.getTitle(),
                b.getSlug(),
                b.getExcerpt(),
                b.getCoverImageUrl(),
                b.getCoverImagePublicId(),
                categorySlug,
                categoryId,
                categoryName,
                b.getReadingTime(),
                b.getStatus(),
                b.getIsPublished(),
                b.getScheduledFor(),
                b.getPublishedAt(),
                b.getSeoTitle(),
                b.getMetaDescription(),
                b.getTags(),
                b.getFocusKeywords());
    }
}
