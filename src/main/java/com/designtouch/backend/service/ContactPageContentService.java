package com.designtouch.backend.service;

import com.designtouch.backend.dto.ContactPageContentRequest;
import com.designtouch.backend.dto.ContactPageContentResponse;
import com.designtouch.backend.entity.ContactPageContent;
import com.designtouch.backend.repository.ContactPageContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactPageContentService {

    private final ContactPageContentRepository repository;
    private final MediaService mediaService;

    @Transactional(readOnly = true)
    public ContactPageContentResponse getContactPageContent() {
        ContactPageContent content = repository.findById((short) 1).orElseGet(this::createDefaultSingleton);
        return ContactPageContentResponse.from(content);
    }

    @Transactional
    public ContactPageContentResponse updateContactPageContent(ContactPageContentRequest request) {
        ContactPageContent content = repository.findById((short) 1).orElseGet(this::createDefaultSingleton);

        // If a new banner image is uploaded and replaces an old one with a different public ID, clean up old asset safely
        if (request.heroBannerImagePublicId() != null
                && content.getHeroBannerImagePublicId() != null
                && !request.heroBannerImagePublicId().equals(content.getHeroBannerImagePublicId())) {
            try {
                mediaService.deleteImage(content.getHeroBannerImagePublicId());
            } catch (Exception e) {
                log.warn("Could not delete previous contact hero banner image {}: {}",
                        content.getHeroBannerImagePublicId(), e.getMessage());
            }
        }

        if (request.heroEyebrow() != null) content.setHeroEyebrow(request.heroEyebrow().trim());
        if (request.heroHeadlineLine1() != null) content.setHeroHeadlineLine1(request.heroHeadlineLine1().trim());
        if (request.heroHeadlineHighlight() != null) content.setHeroHeadlineHighlight(request.heroHeadlineHighlight().trim());
        if (request.heroDescription() != null) content.setHeroDescription(request.heroDescription().trim());
        if (request.badge1Title() != null) content.setBadge1Title(request.badge1Title().trim());
        if (request.badge1Subtitle() != null) content.setBadge1Subtitle(request.badge1Subtitle().trim());
        if (request.badge2Title() != null) content.setBadge2Title(request.badge2Title().trim());
        if (request.badge2Subtitle() != null) content.setBadge2Subtitle(request.badge2Subtitle().trim());
        if (request.heroBannerImageUrl() != null) content.setHeroBannerImageUrl(request.heroBannerImageUrl().trim());
        if (request.heroBannerImagePublicId() != null) content.setHeroBannerImagePublicId(request.heroBannerImagePublicId().trim());
        if (request.heroBannerImageAlt() != null) content.setHeroBannerImageAlt(request.heroBannerImageAlt().trim());
        if (request.heroBannerObjectPosition() != null) content.setHeroBannerObjectPosition(request.heroBannerObjectPosition().trim());

        if (request.directContactEyebrow() != null) content.setDirectContactEyebrow(request.directContactEyebrow().trim());
        if (request.directContactHeading() != null) content.setDirectContactHeading(request.directContactHeading().trim());
        if (request.directContactDescription() != null) content.setDirectContactDescription(request.directContactDescription().trim());
        if (request.directContactEmailLabel() != null) content.setDirectContactEmailLabel(request.directContactEmailLabel().trim());
        if (request.directContactEmail() != null) content.setDirectContactEmail(request.directContactEmail().trim());
        if (request.directContactEmailSub() != null) content.setDirectContactEmailSub(request.directContactEmailSub().trim());
        if (request.directContactPhoneLabel() != null) content.setDirectContactPhoneLabel(request.directContactPhoneLabel().trim());
        if (request.directContactPhone() != null) content.setDirectContactPhone(request.directContactPhone().trim());
        if (request.directContactPhoneSub() != null) content.setDirectContactPhoneSub(request.directContactPhoneSub().trim());
        if (request.directContactLocationLabel() != null) content.setDirectContactLocationLabel(request.directContactLocationLabel().trim());
        if (request.directContactLocation() != null) content.setDirectContactLocation(request.directContactLocation().trim());
        if (request.directContactLocationSub() != null) content.setDirectContactLocationSub(request.directContactLocationSub().trim());
        if (request.directContactWorkingHoursLabel() != null) content.setDirectContactWorkingHoursLabel(request.directContactWorkingHoursLabel().trim());
        if (request.directContactWorkingHours() != null) content.setDirectContactWorkingHours(request.directContactWorkingHours().trim());
        if (request.directContactWorkingHoursSub() != null) content.setDirectContactWorkingHoursSub(request.directContactWorkingHoursSub().trim());

        if (request.directContactEmailEnabled() != null) content.setDirectContactEmailEnabled(request.directContactEmailEnabled());
        if (request.directContactPhoneEnabled() != null) content.setDirectContactPhoneEnabled(request.directContactPhoneEnabled());
        if (request.directContactLocationEnabled() != null) content.setDirectContactLocationEnabled(request.directContactLocationEnabled());
        if (request.directContactWorkingHoursEnabled() != null) content.setDirectContactWorkingHoursEnabled(request.directContactWorkingHoursEnabled());
        if (request.socialLinks() != null) content.setSocialLinks(request.socialLinks().trim());

        content.setUpdatedAt(OffsetDateTime.now());

        ContactPageContent saved = repository.save(content);
        return ContactPageContentResponse.from(saved);
    }

    @Transactional
    public ContactPageContentResponse deleteHeroBannerImage() {
        ContactPageContent content = repository.findById((short) 1).orElseGet(this::createDefaultSingleton);

        if (content.getHeroBannerImagePublicId() != null && !content.getHeroBannerImagePublicId().isBlank()) {
            try {
                mediaService.deleteImage(content.getHeroBannerImagePublicId());
            } catch (Exception e) {
                log.warn("Could not delete contact hero banner image {}: {}",
                        content.getHeroBannerImagePublicId(), e.getMessage());
            }
        }

        content.setHeroBannerImageUrl(null);
        content.setHeroBannerImagePublicId(null);
        content.setUpdatedAt(OffsetDateTime.now());
        ContactPageContent saved = repository.save(content);
        return ContactPageContentResponse.from(saved);
    }

    private ContactPageContent createDefaultSingleton() {
        return repository.save(ContactPageContent.builder()
                .id((short) 1)
                .heroEyebrow("Contact Us")
                .heroHeadlineLine1("Let's Create Something")
                .heroHeadlineHighlight("Amazing.")
                .heroDescription("Have a project in mind? We'd love to hear about it. Fill out the form or reach out to us directly.")
                .badge1Title("Fast Response")
                .badge1Subtitle("We reply within 24 hours")
                .badge2Title("Project Focused")
                .badge2Subtitle("Solutions tailored for you")
                .heroBannerImageUrl(null)
                .heroBannerImagePublicId(null)
                .heroBannerImageAlt("DesignTouch+ Creative Workstation")
                .heroBannerObjectPosition("center")
                .directContactEyebrow("GET IN TOUCH")
                .directContactHeading("DIRECT CONTACT")
                .directContactDescription("Prefer direct communication? Reach out to our team via email, phone, or WhatsApp.")
                .directContactEmailLabel("EMAIL US")
                .directContactEmail("hello@designtouchplus.com")
                .directContactEmailSub("We'll reply within 24 hours")
                .directContactPhoneLabel("CALL / WHATSAPP")
                .directContactPhone("+91 12345 67890")
                .directContactPhoneSub("Mon - Sat: 10:00 AM - 7:00 PM")
                .directContactLocationLabel("OUR LOCATION")
                .directContactLocation("India · Remote Worldwide")
                .directContactLocationSub("We work with clients globally")
                .directContactWorkingHoursLabel("WORKING HOURS")
                .directContactWorkingHours("Monday - Saturday, 10:00 AM - 7:00 PM (IST)")
                .directContactWorkingHoursSub("")
                .directContactEmailEnabled(true)
                .directContactPhoneEnabled(true)
                .directContactLocationEnabled(true)
                .directContactWorkingHoursEnabled(true)
                .socialLinks("[{\"platform\":\"Instagram\",\"url\":\"https://instagram.com/designtouchplus\",\"enabled\":true},{\"platform\":\"YouTube\",\"url\":\"https://youtube.com/@designtouchplus\",\"enabled\":true},{\"platform\":\"LinkedIn\",\"url\":\"https://linkedin.com/company/designtouchplus\",\"enabled\":true}]")
                .updatedAt(OffsetDateTime.now())
                .build());
    }
}

