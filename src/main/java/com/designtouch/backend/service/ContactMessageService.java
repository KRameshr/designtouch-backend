package com.designtouch.backend.service;

import com.designtouch.backend.dto.ContactMessageRequest;
import com.designtouch.backend.dto.ContactMessageResponse;
import com.designtouch.backend.entity.ContactMessage;
import com.designtouch.backend.entity.enums.MessageStatus;
import com.designtouch.backend.exception.EmailDeliveryException;
import com.designtouch.backend.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactMessageService {

    private static final int MAX_NAME_LENGTH = 150;
    private static final int MAX_EMAIL_LENGTH = 150;
    private static final int MAX_COMPANY_LENGTH = 150;
    private static final int MAX_SUBJECT_LENGTH = 250;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private final ContactMessageRepository repository;
    private final EmailService emailService;
    private final ContactSubmissionRateLimiter rateLimiter;

    @Value("${app.contact.recipient-email:designtouchplus@gmail.com}")
    private String recipientEmail = "designtouchplus@gmail.com";

    @Transactional
    public ContactMessageResponse submitEnquiry(ContactMessageRequest request, String clientIp) {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be null.");
        }

        String name = validateAndTrimName(request.name());
        String email = validateAndNormalizeEmail(request.email());
        String message = validateAndTrimMessage(request.message());
        String company = validateAndTrimOptional(request.company(), MAX_COMPANY_LENGTH, "Company");
        String subject = validateAndTrimOptional(request.subject(), MAX_SUBJECT_LENGTH, "Subject");

        // Rate limiting check per client IP
        if (clientIp != null && !rateLimiter.isAllowed("ip:" + clientIp)) {
            log.warn("[ContactMessageService] Rate limit exceeded for IP: {}", clientIp);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Too many enquiry submissions from your network. Please wait a minute before trying again.");
        }

        // Rate limiting check per email
        if (!rateLimiter.isAllowed("email:" + email)) {
            log.warn("[ContactMessageService] Rate limit exceeded for email: {}", email);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Too many enquiry submissions for this email address. Please wait a minute.");
        }

        // Duplicate submission prevention (same email and message content within 30 seconds)
        String deduplicationKey = email + "::" + message;
        if (rateLimiter.isDuplicate(deduplicationKey)) {
            log.warn("[ContactMessageService] Duplicate submission detected for email: {}", email);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "A message with this content was recently received. Please wait a moment before sending another.");
        }

        // Persist enquiry in database
        OffsetDateTime now = OffsetDateTime.now();
        ContactMessage contactMessage = ContactMessage.builder()
                .name(name)
                .email(email)
                .company(company)
                .subject(subject)
                .message(message)
                .status(MessageStatus.NEW)
                .createdAt(now)
                .build();

        ContactMessage saved = repository.save(contactMessage);
        log.info("[ContactMessageService] Stored enquiry ID: {} from visitor: {}", saved.getId(), email);

        // Deliver notification email to configured recipient
        String targetRecipient = (recipientEmail != null && !recipientEmail.isBlank())
                ? recipientEmail.trim()
                : "designtouchplus@gmail.com";

        boolean emailSent = false;
        try {
            emailSent = emailService.sendContactEnquiryEmail(
                    targetRecipient,
                    name,
                    email,
                    company,
                    subject,
                    message,
                    saved.getCreatedAt() != null ? saved.getCreatedAt() : now
            );
        } catch (Exception ex) {
            log.error("[ContactMessageService] Failed to send contact enquiry email: {}", ex.getMessage());
            emailSent = false;
        }

        if (!emailSent) {
            String reason = emailService.getLastErrorDescription();
            log.error("[ContactMessageService] Email delivery failed or was rejected by provider for enquiry ID {}. Reason: {}",
                    saved.getId(), reason != null && !reason.isBlank() ? reason : "Unknown provider failure");
            throw new EmailDeliveryException(
                    "Unable to deliver your enquiry email via our email provider. Please try again or reach out to us directly at "
                            + targetRecipient + "."
            );
        }

        log.info("[ContactMessageService] Successfully processed enquiry ID: {} and delivered notification email to {}",
                saved.getId(), targetRecipient);

        return ContactMessageResponse.of(
                "Thank you! Your message has been sent successfully. We will review your project inquiry and reply within 24 hours.",
                saved.getId()
        );
    }

    private String validateAndTrimName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required.");
        }
        String trimmed = name.trim();
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Name must not exceed " + MAX_NAME_LENGTH + " characters.");
        }
        return trimmed;
    }

    private String validateAndNormalizeEmail(String rawEmail) {
        if (rawEmail == null || rawEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        String normalized = rawEmail.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() > MAX_EMAIL_LENGTH) {
            throw new IllegalArgumentException("Email must not exceed " + MAX_EMAIL_LENGTH + " characters.");
        }
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
        return normalized;
    }

    private String validateAndTrimMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Project description is required.");
        }
        return message.trim();
    }

    private String validateAndTrimOptional(String value, int maxLength, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " must not exceed " + maxLength + " characters.");
        }
        return trimmed;
    }

    public void setRecipientEmail(String email) {
        this.recipientEmail = email;
    }
}

