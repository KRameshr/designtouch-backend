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
@Table(name = "contact_page_content")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactPageContent {

    @Id
    @Builder.Default
    private Short id = 1;

    @Column(name = "hero_eyebrow", length = 150)
    private String heroEyebrow;

    @Column(name = "hero_headline_line1", length = 150)
    private String heroHeadlineLine1;

    @Column(name = "hero_headline_highlight", length = 150)
    private String heroHeadlineHighlight;

    @Column(name = "hero_description", columnDefinition = "TEXT")
    private String heroDescription;

    @Column(name = "badge1_title", length = 100)
    private String badge1Title;

    @Column(name = "badge1_subtitle", length = 150)
    private String badge1Subtitle;

    @Column(name = "badge2_title", length = 100)
    private String badge2Title;

    @Column(name = "badge2_subtitle", length = 150)
    private String badge2Subtitle;

    @Column(name = "hero_banner_image_url", columnDefinition = "TEXT")
    private String heroBannerImageUrl;

    @Column(name = "hero_banner_image_public_id", columnDefinition = "TEXT")
    private String heroBannerImagePublicId;

    @Column(name = "hero_banner_image_alt", length = 255)
    @Builder.Default
    private String heroBannerImageAlt = "DesignTouch+ Creative Workstation";

    @Column(name = "hero_banner_object_position", length = 50)
    @Builder.Default
    private String heroBannerObjectPosition = "center";

    @Column(name = "direct_contact_eyebrow", length = 150)
    @Builder.Default
    private String directContactEyebrow = "GET IN TOUCH";

    @Column(name = "direct_contact_heading", length = 150)
    @Builder.Default
    private String directContactHeading = "DIRECT CONTACT";

    @Column(name = "direct_contact_description", columnDefinition = "TEXT")
    private String directContactDescription;

    @Column(name = "direct_contact_email_label", length = 100)
    @Builder.Default
    private String directContactEmailLabel = "EMAIL US";

    @Column(name = "direct_contact_email", length = 255)
    private String directContactEmail;

    @Column(name = "direct_contact_email_sub", length = 150)
    private String directContactEmailSub;

    @Column(name = "direct_contact_phone_label", length = 100)
    @Builder.Default
    private String directContactPhoneLabel = "CALL / WHATSAPP";

    @Column(name = "direct_contact_phone", length = 100)
    private String directContactPhone;

    @Column(name = "direct_contact_phone_sub", length = 150)
    private String directContactPhoneSub;

    @Column(name = "direct_contact_location_label", length = 100)
    @Builder.Default
    private String directContactLocationLabel = "OUR LOCATION";

    @Column(name = "direct_contact_location", length = 255)
    private String directContactLocation;

    @Column(name = "direct_contact_location_sub", length = 150)
    private String directContactLocationSub;

    @Column(name = "direct_contact_working_hours_label", length = 100)
    @Builder.Default
    private String directContactWorkingHoursLabel = "WORKING HOURS";

    @Column(name = "direct_contact_working_hours", length = 255)
    private String directContactWorkingHours;

    @Column(name = "direct_contact_working_hours_sub", length = 150)
    private String directContactWorkingHoursSub;

    @Column(name = "direct_contact_email_enabled")
    @Builder.Default
    private Boolean directContactEmailEnabled = true;

    @Column(name = "direct_contact_phone_enabled")
    @Builder.Default
    private Boolean directContactPhoneEnabled = true;

    @Column(name = "direct_contact_location_enabled")
    @Builder.Default
    private Boolean directContactLocationEnabled = true;

    @Column(name = "direct_contact_working_hours_enabled")
    @Builder.Default
    private Boolean directContactWorkingHoursEnabled = true;

    @Column(name = "social_links", columnDefinition = "TEXT")
    private String socialLinks;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}

