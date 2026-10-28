package com.designtouch.backend.entity;

import com.designtouch.backend.entity.enums.BlogPostStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "blog_post")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogPost {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 250)
    private String title;

    @Column(nullable = false, unique = true, length = 250)
    private String slug;

    @Column(length = 500)
    private String excerpt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "cover_image_url", columnDefinition = "TEXT")
    private String coverImageUrl;

    @Column(name = "cover_image_public_id", columnDefinition = "TEXT")
    private String coverImagePublicId;

    @Column(name = "seo_title", length = 300)
    private String seoTitle;

    @Column(name = "meta_description", columnDefinition = "TEXT")
    private String metaDescription;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tags", columnDefinition = "text[]")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "focus_keywords", columnDefinition = "text[]")
    @Builder.Default
    private List<String> focusKeywords = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private BlogPostStatus status = BlogPostStatus.PUBLISHED;

    @Column(name = "scheduled_for")
    private OffsetDateTime scheduledFor;

    // Dynamic database-backed category association
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private BlogCategory categoryRef;

    // Category slug for backward compatibility and fast direct queries
    @Column(name = "category", length = 100)
    private String category;

    // ON DELETE SET NULL in the DB — deleting a team member keeps their posts,
    // just clears the author link. fetch = LAZY so listing blogs doesn't always
    // pull the full author row unless explicitly requested.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private TeamMember author;

    @Column(name = "reading_time")
    private Integer readingTime;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private Boolean isPublished = true;

    @Column(name = "published_at", nullable = false)
    private OffsetDateTime publishedAt;

    @PrePersist
    protected void onCreate() {
        syncPublicationState();
        if (publishedAt == null && status == BlogPostStatus.PUBLISHED) {
            publishedAt = OffsetDateTime.now();
        } else if (publishedAt == null) {
            publishedAt = OffsetDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        syncPublicationState();
    }

    private void syncPublicationState() {
        if (status == null) {
            status = Boolean.TRUE.equals(isPublished) ? BlogPostStatus.PUBLISHED : BlogPostStatus.DRAFT;
        }
        if (status == BlogPostStatus.PUBLISHED) {
            isPublished = true;
        } else {
            isPublished = false;
        }
    }
}
