package com.designtouch.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Singleton table — exactly one row (id always = 1, enforced by a DB CHECK
 * constraint in V10__create_portfolio_page_content.sql). Holds the Public Portfolio
 * intro header copy and showcase video.
 */
@Entity
@Table(name = "portfolio_page_content")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioPageContent {

    @Id
    @Builder.Default
    private Short id = 1;

    @Column(length = 150)
    private String eyebrow;

    @Column(name = "headline_line1", length = 150)
    private String headlineLine1;

    @Column(name = "headline_line2", length = 150)
    private String headlineLine2;

    @Column(name = "headline_highlight", length = 150)
    private String headlineHighlight;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "intro_video_url", columnDefinition = "TEXT")
    private String introVideoUrl;

    @Column(name = "intro_video_public_id", columnDefinition = "TEXT")
    private String introVideoPublicId;

    @Column(name = "updated_at")
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}

