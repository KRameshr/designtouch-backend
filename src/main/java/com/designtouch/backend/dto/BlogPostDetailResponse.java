package com.designtouch.backend.dto;

import com.designtouch.backend.entity.BlogPost;
import com.designtouch.backend.entity.enums.BlogCategory;

import java.time.OffsetDateTime;
import java.util.UUID;

// Full version for the single blog post page — includes body content + author name
// (author is fetched inside the @Transactional service method, never lazy-loaded
// after the transaction closes, which is why this is a flattened String and not
// the full TeamMember entity/DTO)
public record BlogPostDetailResponse(
        UUID id, String title, String slug, String excerpt, String content, String coverImageUrl,
        BlogCategory category, String authorName, Integer readingTime, Integer viewCount,
        OffsetDateTime publishedAt) {

    public static BlogPostDetailResponse from(BlogPost b) {
        String authorName = b.getAuthor() != null ? b.getAuthor().getName() : null;
        return new BlogPostDetailResponse(
                b.getId(), b.getTitle(), b.getSlug(), b.getExcerpt(), b.getContent(), b.getCoverImageUrl(),
                b.getCategory(), authorName, b.getReadingTime(), b.getViewCount(), b.getPublishedAt());
    }
}
