package com.designtouch.backend.entity;

import com.designtouch.backend.entity.enums.ContentType;
import com.designtouch.backend.entity.enums.PortfolioCategory;
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
@Table(name = "portfolio_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioItem {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, unique = true, length = 250)
    private String slug;

    // Maps to Postgres native enum `content_type`
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "content_type", nullable = false, columnDefinition = "content_type")
    private ContentType contentType;

    // Dynamic database-backed category association
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private com.designtouch.backend.entity.PortfolioCategory categoryRef;

    // Legacy Postgres native enum `portfolio_category` — maintained for backward compatibility
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "portfolio_category")
    private PortfolioCategory category;

    @Column(name = "sub_category", length = 150)
    private String subCategory;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 20)
    private String duration;

    @Column(name = "video_url", columnDefinition = "TEXT")
    private String videoUrl;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(name = "video_public_id", columnDefinition = "TEXT")
    private String videoPublicId;

    @Column(name = "image_public_id", columnDefinition = "TEXT")
    private String imagePublicId;

    @Column(name = "live_video_url", columnDefinition = "TEXT")
    private String liveVideoUrl;

    @Column(name = "google_drive_url", columnDefinition = "TEXT")
    private String googleDriveUrl;

    @Column(name = "video_aspect_ratio", length = 20)
    @Builder.Default
    private com.designtouch.backend.entity.enums.VideoAspectRatio videoAspectRatio = com.designtouch.backend.entity.enums.VideoAspectRatio.AUTO;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resource_links", columnDefinition = "jsonb")
    @Builder.Default
    private java.util.List<com.designtouch.backend.dto.ResourceLink> resourceLinks = new java.util.ArrayList<>();

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (slug == null || slug.isBlank()) {
            String base = (title != null && !title.isBlank())
                    ? title.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "")
                    : "project";
            if (base.isBlank()) {
                base = "project";
            }
            slug = base + "-" + UUID.randomUUID().toString().substring(0, 8);
        }
    }
}
