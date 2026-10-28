package com.designtouch.backend.dto;

import com.designtouch.backend.entity.BlogPost;
import com.designtouch.backend.entity.enums.BlogPostStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

// Full version for the single blog post page — includes body content + author name + SEO fields
public record BlogPostDetailResponse(
        UUID id,
        String title,
        String slug,
        String excerpt,
        String content,
        String coverImageUrl,
        String coverImagePublicId,
        String category,
        UUID categoryId,
        String categoryName,
        String authorName,
        Integer readingTime,
        Integer viewCount,
        BlogPostStatus status,
        Boolean isPublished,
        OffsetDateTime scheduledFor,
        OffsetDateTime publishedAt,
        String seoTitle,
        String metaDescription,
        List<String> tags,
        List<String> focusKeywords) {

    public static BlogPostDetailResponse from(BlogPost b) {
        String authorName = b.getAuthor() != null ? b.getAuthor().getName() : null;
        UUID categoryId = b.getCategoryRef() != null ? b.getCategoryRef().getId() : null;
        String categoryName = b.getCategoryRef() != null ? b.getCategoryRef().getName() : b.getCategory();
        String categorySlug = b.getCategoryRef() != null ? b.getCategoryRef().getSlug() : b.getCategory();

        return new BlogPostDetailResponse(
                b.getId(),
                b.getTitle(),
                b.getSlug(),
                b.getExcerpt(),
                b.getContent(),
                b.getCoverImageUrl(),
                b.getCoverImagePublicId(),
                categorySlug,
                categoryId,
                categoryName,
                authorName,
                b.getReadingTime(),
                b.getViewCount(),
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
