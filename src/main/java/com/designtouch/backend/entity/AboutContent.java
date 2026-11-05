package com.designtouch.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Singleton table — exactly one row (id always = 1, enforced by a DB CHECK
 * constraint in V1__create_tables.sql). Holds the About page's editable text.
 */
@Entity
@Table(name = "about_content")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AboutContent {

    @Id
    @Builder.Default
    private Short id = 1;

    @Column(length = 200)
    private String heading;

    @Column(columnDefinition = "TEXT")
    private String subheading;

    @Column(name = "cta_text", length = 100)
    private String ctaText;

    @Column(name = "cta_link", length = 200)
    private String ctaLink;

    @Column(name = "hero_banner_image_url", columnDefinition = "TEXT")
    private String heroBannerImageUrl;

    @Column(name = "hero_banner_image_public_id", columnDefinition = "TEXT")
    private String heroBannerImagePublicId;

    @Column(name = "story_heading", length = 200)
    private String storyHeading;

    @Column(name = "story_image_url", columnDefinition = "TEXT")
    private String storyImageUrl;

    @Column(name = "story_paragraph_1", columnDefinition = "TEXT")
    private String storyParagraph1;

    @Column(name = "story_paragraph_2", columnDefinition = "TEXT")
    private String storyParagraph2;

    @Column(name = "founder_name", length = 150)
    private String founderName;

    @Column(name = "founder_designation", length = 150)
    private String founderDesignation;

    @Column(name = "founder_signature_url", columnDefinition = "TEXT")
    private String founderSignatureUrl;

    @Column(name = "story_video_url", columnDefinition = "TEXT")
    private String storyVideoUrl;

    @Column(name = "story_video_thumbnail_url", columnDefinition = "TEXT")
    private String storyVideoThumbnailUrl;
}
