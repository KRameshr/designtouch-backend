package com.designtouch.backend.entity;

import com.designtouch.backend.entity.enums.BlogCategory;
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

    // Maps to Postgres native enum `blog_category`
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "blog_category")
    private BlogCategory category;

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
        if (publishedAt == null) {
            publishedAt = OffsetDateTime.now();
        }
    }
}
