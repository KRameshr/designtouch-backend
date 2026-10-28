package com.designtouch.backend.service;

import com.designtouch.backend.dto.NewsletterResponse;
import com.designtouch.backend.entity.NewsletterSubscriber;
import com.designtouch.backend.exception.DuplicateSubscriptionException;
import com.designtouch.backend.repository.NewsletterSubscriberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Proxy;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NewsletterServiceTest {

    private final Map<UUID, NewsletterSubscriber> database = new HashMap<>();

    private NewsletterSubscriberRepository repository;
    private EmailService emailService;
    private NewsletterRateLimiter rateLimiter;
    private NewsletterService service;

    private boolean emailServiceCalled = false;
    private boolean emailServiceReturnVal = true;
    private boolean emailServiceThrowException = false;

    @BeforeEach
    void setUp() {
        database.clear();
        emailServiceCalled = false;
        emailServiceReturnVal = true;
        emailServiceThrowException = false;

        // In-memory mock repository via dynamic proxy
        repository = (NewsletterSubscriberRepository) Proxy.newProxyInstance(
                NewsletterSubscriberRepository.class.getClassLoader(),
                new Class<?>[]{NewsletterSubscriberRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("existsByEmailIgnoreCase".equals(name) || "existsByEmail".equals(name)) {
                        String email = (String) args[0];
                        return database.values().stream()
                                .anyMatch(s -> s.getEmail() != null && s.getEmail().equalsIgnoreCase(email));
                    }
                    if ("findByEmailIgnoreCase".equals(name)) {
                        String email = (String) args[0];
                        return database.values().stream()
                                .filter(s -> s.getEmail() != null && s.getEmail().equalsIgnoreCase(email))
                                .findFirst();
                    }
                    if ("save".equals(name)) {
                        NewsletterSubscriber sub = (NewsletterSubscriber) args[0];
                        if (sub.getId() == null) {
                            sub.setId(UUID.randomUUID());
                        }
                        database.put(sub.getId(), sub);
                        return sub;
                    }
                    if ("count".equals(name)) {
                        return (long) database.size();
                    }
                    return null;
                }
        );

        rateLimiter = new NewsletterRateLimiter();
        rateLimiter.reset();

        // Stub EmailService that mocks email sending without network calls
        emailService = new EmailService(
                "re_mock_api_key",
                "DesignTouch+ <onboarding@resend.dev>",
                true,
                null,
                null
        ) {
            @Override
            public boolean sendWelcomeEmail(String toEmail) {
                emailServiceCalled = true;
                if (emailServiceThrowException) {
                    throw new RuntimeException("Simulated Resend API timeout");
                }
                return emailServiceReturnVal;
            }
        };

        service = new NewsletterService(repository, emailService, rateLimiter);
    }

    @Test
    @DisplayName("Valid new email subscription successfully persists and returns 201")
    void subscribe_withValidNewEmail_persistsAndReturns201Created() {
        NewsletterResponse response = service.subscribe("creator@example.com", "192.168.1.10");

        assertThat(response.status()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.email()).isEqualTo("creator@example.com");
        assertThat(response.emailSent()).isTrue();
        assertThat(database).hasSize(1);
        assertThat(emailServiceCalled).isTrue();

        NewsletterSubscriber saved = database.values().iterator().next();
        assertThat(saved.getEmail()).isEqualTo("creator@example.com");
        assertThat(saved.getIsActive()).isTrue();
        assertThat(saved.getSubscribedAt()).isNotNull();
    }

    @Test
    @DisplayName("Email normalization trims whitespace and downcases to lowercase")
    void subscribe_withUppercaseAndWhitespace_normalizesCorrectly() {
        NewsletterResponse response = service.subscribe("   CREATOR.Pro@DesignTouch.COM  ", "192.168.1.11");

        assertThat(response.email()).isEqualTo("creator.pro@designtouch.com");
        NewsletterSubscriber saved = database.values().iterator().next();
        assertThat(saved.getEmail()).isEqualTo("creator.pro@designtouch.com");
    }

    @Test
    @DisplayName("Duplicate email subscription throws DuplicateSubscriptionException (409 Conflict)")
    void subscribe_withExistingEmail_throwsDuplicateSubscriptionException() {
        // Pre-populate with lowercase email and confirmationSent = true
        UUID id = UUID.randomUUID();
        database.put(id, NewsletterSubscriber.builder()
                .id(id)
                .email("existing@example.com")
                .isActive(true)
                .confirmationSent(true)
                .build());

        // Attempt duplicate with mixed case
        assertThatThrownBy(() -> service.subscribe("Existing@Example.COM", "192.168.1.12"))
                .isInstanceOf(DuplicateSubscriptionException.class)
                .hasMessageContaining("already subscribed");

        assertThat(database).hasSize(1);
        assertThat(emailServiceCalled).isFalse();
    }

    @Test
    @DisplayName("Repeated submissions safely reject duplicates and keep database count at 1")
    void subscribe_withRepeatedSubmissions_doesNotCreateDuplicateRecords() {
        NewsletterResponse first = service.subscribe("repeat@example.com", "10.0.0.1");
        assertThat(first.status()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(database).hasSize(1);
        assertThat(database.values().iterator().next().getConfirmationSent()).isTrue();

        assertThatThrownBy(() -> service.subscribe("repeat@example.com", "10.0.0.1"))
                .isInstanceOf(DuplicateSubscriptionException.class);

        assertThat(database).hasSize(1);
    }

    @Test
    @DisplayName("Existing subscriber with confirmationSent=false safely retries and returns 200 OK")
    void subscribe_whenExistingSubscriberHasConfirmationSentFalse_retriesAndSendsEmail() {
        UUID id = UUID.randomUUID();
        NewsletterSubscriber existing = NewsletterSubscriber.builder()
                .id(id)
                .email("kramesh9652@gmail.com")
                .isActive(true)
                .confirmationSent(false)
                .subscribedAt(java.time.OffsetDateTime.now().minusDays(1))
                .build();
        database.put(id, existing);

        NewsletterResponse response = service.subscribe("kramesh9652@gmail.com", "192.168.1.20");

        assertThat(response.status()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.emailSent()).isTrue();
        assertThat(response.message()).contains("Confirmation email sent");
        assertThat(emailServiceCalled).isTrue();

        NewsletterSubscriber updated = database.get(id);
        assertThat(updated.getConfirmationSent()).isTrue();
        assertThat(updated.getConfirmationSentAt()).isNotNull();
        assertThat(database).hasSize(1);
    }

    @Test
    @DisplayName("Existing subscriber retry does not mark confirmationSent if email provider rejects")
    void subscribe_whenExistingSubscriberRetryFails_doesNotMarkConfirmationSent() {
        emailServiceReturnVal = false; // Simulated Resend rejection or not configured

        UUID id = UUID.randomUUID();
        NewsletterSubscriber existing = NewsletterSubscriber.builder()
                .id(id)
                .email("kramesh9652@gmail.com")
                .isActive(true)
                .confirmationSent(false)
                .subscribedAt(java.time.OffsetDateTime.now().minusDays(1))
                .build();
        database.put(id, existing);

        assertThatThrownBy(() -> service.subscribe("kramesh9652@gmail.com", "192.168.1.21"))
                .isInstanceOf(DuplicateSubscriptionException.class)
                .hasMessageContaining("could not be completed");

        assertThat(emailServiceCalled).isTrue();
        NewsletterSubscriber current = database.get(id);
        assertThat(current.getConfirmationSent()).isFalse();
        assertThat(current.getConfirmationSentAt()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "plainaddress", "missing@domain", "@nodomain.com", "user@.com", "user @domain.com"})
    @DisplayName("Invalid email formats throw IllegalArgumentException (400 Bad Request)")
    void subscribe_withInvalidEmail_throwsIllegalArgumentException(String invalidEmail) {
        assertThatThrownBy(() -> service.subscribe(invalidEmail, "192.168.1.14"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valid email");

        assertThat(database).isEmpty();
        assertThat(emailServiceCalled).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t\n"})
    @DisplayName("Missing or blank email throws IllegalArgumentException (400 Bad Request)")
    void subscribe_withMissingOrBlankEmail_throwsIllegalArgumentException(String blankEmail) {
        assertThatThrownBy(() -> service.subscribe(blankEmail, "192.168.1.15"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email is required");

        assertThatThrownBy(() -> service.subscribe(null, "192.168.1.15"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email is required");

        assertThat(database).isEmpty();
    }

    @Test
    @DisplayName("Email exceeding 150 characters throws IllegalArgumentException")
    void subscribe_withOversizedEmail_throwsIllegalArgumentException() {
        String longEmail = "a".repeat(140) + "@longdomain.com"; // > 150 chars

        assertThatThrownBy(() -> service.subscribe(longEmail, "192.168.1.16"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exceed 150 characters");

        assertThat(database).isEmpty();
    }

    @Test
    @DisplayName("Database unique constraint race condition is handled and remapped to DuplicateSubscriptionException")
    void subscribe_whenDatabaseThrowsDataIntegrityViolation_throwsDuplicateSubscriptionException() {
        NewsletterSubscriberRepository failingRepo = (NewsletterSubscriberRepository) Proxy.newProxyInstance(
                NewsletterSubscriberRepository.class.getClassLoader(),
                new Class<?>[]{NewsletterSubscriberRepository.class},
                (proxy, method, args) -> {
                    if ("findByEmailIgnoreCase".equals(method.getName())) {
                        return Optional.empty(); // passes initial check
                    }
                    if ("existsByEmailIgnoreCase".equals(method.getName())) {
                        return false;
                    }
                    if ("save".equals(method.getName())) {
                        throw new DataIntegrityViolationException("duplicate key value violates unique constraint 'newsletter_subscriber_email_key'");
                    }
                    return null;
                }
        );

        NewsletterService failingService = new NewsletterService(failingRepo, emailService, rateLimiter);

        assertThatThrownBy(() -> failingService.subscribe("concurrent@example.com", "192.168.1.17"))
                .isInstanceOf(DuplicateSubscriptionException.class)
                .hasMessageContaining("already subscribed");
    }

    @Test
    @DisplayName("Email provider failure does not roll back or fail database subscription")
    void subscribe_whenEmailProviderFails_handlesSafelyAndPersistsSubscriber() {
        emailServiceReturnVal = false; // Provider returns false (rejected)

        NewsletterResponse response = service.subscribe("providerfail@example.com", "192.168.1.18");

        assertThat(response.status()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.emailSent()).isFalse();
        assertThat(database).hasSize(1);
    }

    @Test
    @DisplayName("Email provider unexpected exception does not crash subscription flow")
    void subscribe_whenEmailProviderThrows_handlesSafelyAndPersistsSubscriber() {
        emailServiceThrowException = true; // Simulating socket timeout or connection refused

        NewsletterResponse response = service.subscribe("timeout@example.com", "192.168.1.19");

        assertThat(response.status()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.emailSent()).isFalse();
        assertThat(database).hasSize(1);
    }

    @Test
    @DisplayName("Excessive requests exceed rate limit and throw 429 Too Many Requests")
    void subscribe_whenRateLimitExceeded_throws429TooManyRequests() {
        String testIp = "203.0.113.42";

        // Perform 5 allowed requests
        for (int i = 0; i < 5; i++) {
            service.subscribe("subscriber" + i + "@example.com", testIp);
        }
        assertThat(database).hasSize(5);

        // 6th request from same IP should be blocked
        assertThatThrownBy(() -> service.subscribe("subscriber6@example.com", testIp))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
                });
    }
}

