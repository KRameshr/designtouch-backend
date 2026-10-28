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
@Table(name = "pricing_page_content")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingPageContent {

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

    @Column(name = "hero_highlight_style", length = 50)
    @Builder.Default
    private String heroHighlightStyle = "gradient";

    @Column(name = "hero_description", columnDefinition = "TEXT")
    private String heroDescription;

    // Direct banner image URL (Cloudinary, external, or local)
    @Column(name = "hero_banner_image_url", columnDefinition = "TEXT")
    private String heroBannerImageUrl;

    @Column(name = "hero_banner_image_public_id", columnDefinition = "TEXT")
    private String heroBannerImagePublicId;

    @Column(name = "hero_banner_image_alt", length = 255)
    @Builder.Default
    private String heroBannerImageAlt = "DesignTouch+ Creative Production Studio";

    @Column(name = "hero_banner_object_position", length = 50)
    @Builder.Default
    private String heroBannerObjectPosition = "center";

    // Action CTAs
    @Column(name = "hero_primary_cta_text", length = 100)
    @Builder.Default
    private String heroPrimaryCtaText = "CHOOSE A PLAN";

    @Column(name = "hero_primary_cta_link", length = 200)
    @Builder.Default
    private String heroPrimaryCtaLink = "#plans";

    @Column(name = "hero_secondary_cta_text", length = 100)
    @Builder.Default
    private String heroSecondaryCtaText = "BOOK A CALL";

    @Column(name = "hero_secondary_cta_link", length = 200)
    @Builder.Default
    private String heroSecondaryCtaLink = "/contact";

    // Preserved for backwards compatibility
    @Column(name = "hero_background_image_url", columnDefinition = "TEXT")
    private String heroBackgroundImageUrl;

    @Column(name = "updated_at")
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}
