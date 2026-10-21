package com.designtouch.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Singleton table — exactly one row (id always = 1, enforced by a DB CHECK
 * constraint in V8__create_hero_content.sql). Holds the Homepage Hero editable content.
 */
@Entity
@Table(name = "hero_content")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeroContent {

    @Id
    @Builder.Default
    private Short id = 1;

    @Column(length = 150)
    private String eyebrow;

    @Column(name = "headline_line1", length = 150)
    private String headlineLine1;

    @Column(name = "headline_line2", length = 150)
    private String headlineLine2;

    @Column(name = "headline_line3", length = 150)
    private String headlineLine3;

    @Column(name = "headline_highlight", length = 150)
    private String headlineHighlight;

    @Column(columnDefinition = "TEXT")
    private String subheading;

    @Column(name = "primary_cta_text", length = 100)
    private String primaryCtaText;

    @Column(name = "primary_cta_link", length = 200)
    private String primaryCtaLink;

    @Column(name = "secondary_cta_text", length = 100)
    private String secondaryCtaText;

    @Column(name = "secondary_cta_link", length = 200)
    private String secondaryCtaLink;

    @Column(name = "video_url", columnDefinition = "TEXT")
    private String videoUrl;

    @Column(name = "badge_text", length = 100)
    private String badgeText;

    @Column(name = "spec_badge_text", length = 100)
    private String specBadgeText;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        this.updatedAt = OffsetDateTime.now();
    }
}

