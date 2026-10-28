package com.designtouch.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "blog_page_content")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogPageContent {

    @Id
    @Builder.Default
    private Short id = 1;

    @Column(name = "hero_eyebrow", length = 150)
    private String heroEyebrow;

    @Column(name = "hero_headline_line1", length = 150)
    private String heroHeadlineLine1;

    @Column(name = "hero_headline_line2", length = 150)
    private String heroHeadlineLine2;

    @Column(name = "hero_headline_highlight", length = 150)
    private String heroHeadlineHighlight;

    @Column(name = "hero_description", columnDefinition = "TEXT")
    private String heroDescription;

    @Column(name = "hero_cta_text", length = 100)
    @Builder.Default
    private String heroCtaText = "EXPLORE ALL ARTICLES";

    @Column(name = "hero_cta_link", length = 200)
    @Builder.Default
    private String heroCtaLink = "#articles";

    @Column(name = "hero_banner_image_url", columnDefinition = "TEXT")
    private String heroBannerImageUrl;

    @Column(name = "hero_banner_image_public_id", columnDefinition = "TEXT")
    private String heroBannerImagePublicId;

    @Column(name = "hero_banner_image_alt", length = 255)
    @Builder.Default
    private String heroBannerImageAlt = "DesignTouch+ Creative Workspace Studio";

    @Column(name = "hero_banner_object_position", length = 50)
    @Builder.Default
    private String heroBannerObjectPosition = "center right";

    @Column(name = "updated_at")
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}
