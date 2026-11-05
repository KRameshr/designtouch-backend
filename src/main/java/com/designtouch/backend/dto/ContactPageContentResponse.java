package com.designtouch.backend.dto;

import com.designtouch.backend.entity.ContactPageContent;

public record ContactPageContentResponse(
        Short id,
        String heroEyebrow,
        String heroHeadlineLine1,
        String heroHeadlineHighlight,
        String heroDescription,
        String badge1Title,
        String badge1Subtitle,
        String badge2Title,
        String badge2Subtitle,
        String heroBannerImageUrl,
        String heroBannerImagePublicId,
        String heroBannerImageAlt,
        String heroBannerObjectPosition,
        String directContactEyebrow,
        String directContactHeading,
        String directContactDescription,
        String directContactEmailLabel,
        String directContactEmail,
        String directContactEmailSub,
        String directContactPhoneLabel,
        String directContactPhone,
        String directContactPhoneSub,
        String directContactLocationLabel,
        String directContactLocation,
        String directContactLocationSub,
        String directContactWorkingHoursLabel,
        String directContactWorkingHours,
        String directContactWorkingHoursSub,
        Boolean directContactEmailEnabled,
        Boolean directContactPhoneEnabled,
        Boolean directContactLocationEnabled,
        Boolean directContactWorkingHoursEnabled,
        String socialLinks
) {
    public static ContactPageContentResponse from(ContactPageContent c) {
        if (c == null) {
            return fallback();
        }
        return new ContactPageContentResponse(
                c.getId(),
                c.getHeroEyebrow() != null ? c.getHeroEyebrow() : "Contact Us",
                c.getHeroHeadlineLine1() != null ? c.getHeroHeadlineLine1() : "Let's Create Something",
                c.getHeroHeadlineHighlight() != null ? c.getHeroHeadlineHighlight() : "Amazing.",
                c.getHeroDescription() != null ? c.getHeroDescription() : "Have a project in mind? We'd love to hear about it. Fill out the form or reach out to us directly.",
                c.getBadge1Title() != null ? c.getBadge1Title() : "Fast Response",
                c.getBadge1Subtitle() != null ? c.getBadge1Subtitle() : "We reply within 24 hours",
                c.getBadge2Title() != null ? c.getBadge2Title() : "Project Focused",
                c.getBadge2Subtitle() != null ? c.getBadge2Subtitle() : "Solutions tailored for you",
                c.getHeroBannerImageUrl(),
                c.getHeroBannerImagePublicId(),
                c.getHeroBannerImageAlt() != null ? c.getHeroBannerImageAlt() : "DesignTouch+ Creative Workstation",
                c.getHeroBannerObjectPosition() != null ? c.getHeroBannerObjectPosition() : "center",
                c.getDirectContactEyebrow() != null ? c.getDirectContactEyebrow() : "GET IN TOUCH",
                c.getDirectContactHeading() != null ? c.getDirectContactHeading() : "DIRECT CONTACT",
                c.getDirectContactDescription() != null ? c.getDirectContactDescription() : "Prefer direct communication? Reach out to our team via email, phone, or WhatsApp.",
                c.getDirectContactEmailLabel() != null ? c.getDirectContactEmailLabel() : "EMAIL US",
                c.getDirectContactEmail() != null ? c.getDirectContactEmail() : "hello@designtouchplus.com",
                c.getDirectContactEmailSub() != null ? c.getDirectContactEmailSub() : "We'll reply within 24 hours",
                c.getDirectContactPhoneLabel() != null ? c.getDirectContactPhoneLabel() : "CALL / WHATSAPP",
                c.getDirectContactPhone() != null ? c.getDirectContactPhone() : "+91 12345 67890",
                c.getDirectContactPhoneSub() != null ? c.getDirectContactPhoneSub() : "Mon - Sat: 10:00 AM - 7:00 PM",
                c.getDirectContactLocationLabel() != null ? c.getDirectContactLocationLabel() : "OUR LOCATION",
                c.getDirectContactLocation() != null ? c.getDirectContactLocation() : "India · Remote Worldwide",
                c.getDirectContactLocationSub() != null ? c.getDirectContactLocationSub() : "We work with clients globally",
                c.getDirectContactWorkingHoursLabel() != null ? c.getDirectContactWorkingHoursLabel() : "WORKING HOURS",
                c.getDirectContactWorkingHours() != null ? c.getDirectContactWorkingHours() : "Monday - Saturday, 10:00 AM - 7:00 PM (IST)",
                c.getDirectContactWorkingHoursSub() != null ? c.getDirectContactWorkingHoursSub() : "",
                c.getDirectContactEmailEnabled() != null ? c.getDirectContactEmailEnabled() : true,
                c.getDirectContactPhoneEnabled() != null ? c.getDirectContactPhoneEnabled() : true,
                c.getDirectContactLocationEnabled() != null ? c.getDirectContactLocationEnabled() : true,
                c.getDirectContactWorkingHoursEnabled() != null ? c.getDirectContactWorkingHoursEnabled() : true,
                c.getSocialLinks() != null ? c.getSocialLinks() : "[{\"platform\":\"Instagram\",\"url\":\"https://instagram.com/designtouchplus\",\"enabled\":true},{\"platform\":\"YouTube\",\"url\":\"https://youtube.com/@designtouchplus\",\"enabled\":true},{\"platform\":\"LinkedIn\",\"url\":\"https://linkedin.com/company/designtouchplus\",\"enabled\":true}]"
        );
    }

    public static ContactPageContentResponse fallback() {
        return new ContactPageContentResponse(
                (short) 1,
                "Contact Us",
                "Let's Create Something",
                "Amazing.",
                "Have a project in mind? We'd love to hear about it. Fill out the form or reach out to us directly.",
                "Fast Response",
                "We reply within 24 hours",
                "Project Focused",
                "Solutions tailored for you",
                null,
                null,
                "DesignTouch+ Creative Workstation",
                "center",
                "GET IN TOUCH",
                "DIRECT CONTACT",
                "Prefer direct communication? Reach out to our team via email, phone, or WhatsApp.",
                "EMAIL US",
                "hello@designtouchplus.com",
                "We'll reply within 24 hours",
                "CALL / WHATSAPP",
                "+91 12345 67890",
                "Mon - Sat: 10:00 AM - 7:00 PM",
                "OUR LOCATION",
                "India · Remote Worldwide",
                "We work with clients globally",
                "WORKING HOURS",
                "Monday - Saturday, 10:00 AM - 7:00 PM (IST)",
                "",
                true,
                true,
                true,
                true,
                "[{\"platform\":\"Instagram\",\"url\":\"https://instagram.com/designtouchplus\",\"enabled\":true},{\"platform\":\"YouTube\",\"url\":\"https://youtube.com/@designtouchplus\",\"enabled\":true},{\"platform\":\"LinkedIn\",\"url\":\"https://linkedin.com/company/designtouchplus\",\"enabled\":true}]"
        );
    }
}

