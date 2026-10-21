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
@Table(name = "services_page_content")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicesPageContent {

    @Id
    @Builder.Default
    private Short id = 1;

    // Hero Section
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

    @Column(name = "hero_primary_cta_text", length = 100)
    private String heroPrimaryCtaText;

    @Column(name = "hero_primary_cta_link", length = 200)
    private String heroPrimaryCtaLink;

    @Column(name = "hero_secondary_cta_text", length = 100)
    private String heroSecondaryCtaText;

    @Column(name = "hero_secondary_cta_link", length = 200)
    private String heroSecondaryCtaLink;

    @Column(name = "hero_video_url", columnDefinition = "TEXT")
    private String heroVideoUrl;

    @Column(name = "hero_banner_image_url", columnDefinition = "TEXT")
    private String heroBannerImageUrl;

    @Column(name = "hero_banner_image_public_id", columnDefinition = "TEXT")
    private String heroBannerImagePublicId;

    @Column(name = "hero_stat1_value", length = 50)
    private String heroStat1Value;

    @Column(name = "hero_stat1_label", length = 100)
    private String heroStat1Label;

    @Column(name = "hero_stat2_value", length = 50)
    private String heroStat2Value;

    @Column(name = "hero_stat2_label", length = 100)
    private String heroStat2Label;

    @Column(name = "hero_stat3_value", length = 50)
    private String heroStat3Value;

    @Column(name = "hero_stat3_label", length = 100)
    private String heroStat3Label;

    @Column(name = "hero_show_stats", nullable = false)
    @Builder.Default
    private Boolean heroShowStats = true;

    @Column(name = "hero_show_workstation", nullable = false)
    @Builder.Default
    private Boolean heroShowWorkstation = true;

    // Section Headers
    @Column(name = "services_eyebrow", length = 150)
    private String servicesEyebrow;

    @Column(name = "services_heading", length = 150)
    private String servicesHeading;

    @Column(name = "services_description", columnDefinition = "TEXT")
    private String servicesDescription;

    @Column(name = "process_eyebrow", length = 150)
    private String processEyebrow;

    @Column(name = "process_heading", length = 150)
    private String processHeading;

    @Column(name = "process_description", columnDefinition = "TEXT")
    private String processDescription;

    @Column(name = "tools_eyebrow", length = 150)
    private String toolsEyebrow;

    @Column(name = "tools_heading", length = 150)
    private String toolsHeading;

    @Column(name = "tools_description", columnDefinition = "TEXT")
    private String toolsDescription;

    // Bottom CTA
    @Column(name = "cta_eyebrow", length = 150)
    private String ctaEyebrow;

    @Column(name = "cta_heading_line1", length = 150)
    private String ctaHeadingLine1;

    @Column(name = "cta_heading_line2", length = 150)
    private String ctaHeadingLine2;

    @Column(name = "cta_heading_highlight", length = 150)
    private String ctaHeadingHighlight;

    @Column(name = "cta_description", columnDefinition = "TEXT")
    private String ctaDescription;

    @Column(name = "cta_button_text", length = 100)
    private String ctaButtonText;

    @Column(name = "cta_button_link", length = 200)
    private String ctaButtonLink;

    @Column(name = "cta_accent_note", length = 150)
    private String ctaAccentNote;

    @Column(name = "updated_at")
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}

