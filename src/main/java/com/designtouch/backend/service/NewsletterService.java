package com.designtouch.backend.service;

import com.designtouch.backend.dto.NewsletterResponse;
import com.designtouch.backend.entity.NewsletterSubscriber;
import com.designtouch.backend.exception.DuplicateSubscriptionException;
import com.designtouch.backend.repository.NewsletterSubscriberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsletterService {

    private static final int MAX_EMAIL_LENGTH = 150;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private final NewsletterSubscriberRepository repository;
    private final EmailService emailService;
    private final NewsletterRateLimiter rateLimiter;

    /**
     * Subscribes an email to the newsletter with normalization, validation,
     * rate limiting, duplicate protection, and welcome email delivery.
     *
     * @param rawEmail The raw input email
     * @param clientIp The client IP address for abuse protection
     * @return NewsletterResponse containing subscription status and metadata
     */
    @Transactional
    public NewsletterResponse subscribe(String rawEmail, String clientIp) {
        String normalizedEmail = validateAndNormalizeEmail(rawEmail);

        // Rate limit verification
        if (clientIp != null && !rateLimiter.isAllowed("ip:" + clientIp)) {
            log.warn("[NewsletterService] Rate limit exceeded for IP: {}", clientIp);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many subscription attempts. Please wait a minute and try again.");
        }
        if (!rateLimiter.isAllowed("email:" + normalizedEmail)) {
            log.warn("[NewsletterService] Rate limit exceeded for email: {}", normalizedEmail);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many subscription attempts for this email. Please wait a minute.");
        }

        // Check for existing subscriber
        Optional<NewsletterSubscriber> existingOpt = repository.findByEmailIgnoreCase(normalizedEmail);
        if (existingOpt.isPresent()) {
            NewsletterSubscriber existing = existingOpt.get();

            // If confirmation email was already successfully accepted, do not send duplicate
            if (Boolean.TRUE.equals(existing.getConfirmationSent())) {
                log.info("[NewsletterService] Subscriber {} already confirmed (confirmation_sent=true). Rejecting duplicate.", normalizedEmail);
                throw new DuplicateSubscriptionException("This email is already subscribed to our newsletter.");
            }

            // Subscriber exists but confirmation email was not recorded as sent (safe retry)
            log.info("[NewsletterService] Subscriber {} exists with unconfirmed delivery. Attempting safe confirmation email retry.", normalizedEmail);
            boolean emailSent = false;
            try {
                emailSent = emailService.sendWelcomeEmail(normalizedEmail);
            } catch (Exception ex) {
                log.error("[NewsletterService] Safe retry sending confirmation email failed for {}: {}", normalizedEmail, ex.getMessage());
            }

            if (emailSent) {
                existing.setConfirmationSent(true);
                existing.setConfirmationSentAt(OffsetDateTime.now());
                repository.save(existing);
                log.info("[NewsletterService] Safe retry succeeded: confirmation sent to existing subscriber {}", normalizedEmail);
                return NewsletterResponse.of(
                        "Confirmation email sent successfully.",
                        HttpStatus.OK.value(),
                        normalizedEmail,
                        true
                );
            } else {
                log.warn("[NewsletterService] Safe retry skipped or rejected by provider for {}", normalizedEmail);
                throw new DuplicateSubscriptionException("This email is already subscribed to our newsletter, but confirmation email delivery could not be completed by Resend (testing restriction: free accounts may only deliver to their registered account address until a domain is verified at resend.com/domains).");
            }
        }

        NewsletterSubscriber subscriber = NewsletterSubscriber.builder()
                .email(normalizedEmail)
                .isActive(true)
                .confirmationSent(false)
                .subscribedAt(OffsetDateTime.now())
                .build();

        try {
            repository.save(subscriber);
            log.info("[NewsletterService] Persisted new subscriber with ID: {}", subscriber.getId());
        } catch (DataIntegrityViolationException ex) {
            log.warn("[NewsletterService] Unique constraint violation on saving email: {}", normalizedEmail);
            throw new DuplicateSubscriptionException("This email is already subscribed to our newsletter.");
        }

        // Attempt transactional email delivery (isolated, failure does not abort subscription)
        boolean emailSent = false;
        try {
            emailSent = emailService.sendWelcomeEmail(normalizedEmail);
            if (emailSent) {
                subscriber.setConfirmationSent(true);
                subscriber.setConfirmationSentAt(OffsetDateTime.now());
                repository.save(subscriber);
                log.info("[NewsletterService] Confirmation marked sent for new subscriber {}", normalizedEmail);
            }
        } catch (Exception ex) {
            log.error("[NewsletterService] Unexpected error invoking EmailService for {}: {}", normalizedEmail, ex.getMessage());
        }

        String message = emailSent
                ? "Successfully subscribed to the newsletter."
                : "Subscription registered successfully, but confirmation email could not be delivered by the email provider.";

        return NewsletterResponse.of(
                message,
                HttpStatus.CREATED.value(),
                normalizedEmail,
                emailSent
        );
    }

    /**
     * Normalizes and validates the email address according to RFC standards.
     */
    public String validateAndNormalizeEmail(String rawEmail) {
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
}

