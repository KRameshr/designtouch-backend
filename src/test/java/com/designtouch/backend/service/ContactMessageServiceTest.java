package com.designtouch.backend.service;

import com.designtouch.backend.dto.ContactMessageRequest;
import com.designtouch.backend.dto.ContactMessageResponse;
import com.designtouch.backend.entity.ContactMessage;
import com.designtouch.backend.exception.EmailDeliveryException;
import com.designtouch.backend.repository.ContactMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Proxy;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContactMessageServiceTest {

    private ContactMessageRepository repository;
    private EmailService emailService;
    private ContactSubmissionRateLimiter rateLimiter;
    private ContactMessageService service;

    private AtomicBoolean emailServiceCalled;
    private AtomicBoolean emailServiceReturnValue;
    private AtomicBoolean emailServiceShouldThrow;
    private AtomicReference<ContactMessage> savedEntity;
    private AtomicReference<String> capturedRecipient;
    private AtomicReference<String> capturedVisitorEmail;
    private AtomicReference<String> capturedSubject;
    private AtomicReference<String> capturedMessage;

    @BeforeEach
    void setUp() {
        emailServiceCalled = new AtomicBoolean(false);
        emailServiceReturnValue = new AtomicBoolean(true);
        emailServiceShouldThrow = new AtomicBoolean(false);
        savedEntity = new AtomicReference<>(null);
        capturedRecipient = new AtomicReference<>(null);
        capturedVisitorEmail = new AtomicReference<>(null);
        capturedSubject = new AtomicReference<>(null);
        capturedMessage = new AtomicReference<>(null);

        rateLimiter = new ContactSubmissionRateLimiter();
        rateLimiter.reset();

        // Dynamic proxy for repository stub
        repository = (ContactMessageRepository) Proxy.newProxyInstance(
                ContactMessageRepository.class.getClassLoader(),
                new Class<?>[]{ContactMessageRepository.class},
                (proxy, method, args) -> {
                    if ("save".equals(method.getName())) {
                        ContactMessage msg = (ContactMessage) args[0];
                        if (msg.getId() == null) {
                            msg.setId(UUID.randomUUID());
                        }
                        if (msg.getCreatedAt() == null) {
                            msg.setCreatedAt(OffsetDateTime.now());
                        }
                        savedEntity.set(msg);
                        return msg;
                    }
                    if ("hashCode".equals(method.getName())) return 42;
                    if ("equals".equals(method.getName())) return proxy == args[0];
                    return null;
                }
        );

        // Subclass EmailService with mocked sendContactEnquiryEmail so NO real network calls are made
        emailService = new EmailService("re_mock_key", "DesignTouch+ <onboarding@resend.dev>", true, null, null) {
            @Override
            public boolean sendContactEnquiryEmail(
                    String recipientEmail,
                    String visitorName,
                    String visitorEmail,
                    String company,
                    String subject,
                    String message,
                    OffsetDateTime submittedAt
            ) {
                emailServiceCalled.set(true);
                capturedRecipient.set(recipientEmail);
                capturedVisitorEmail.set(visitorEmail);
                capturedSubject.set(subject);
                capturedMessage.set(message);

                if (emailServiceShouldThrow.get()) {
                    throw new RuntimeException("Simulated provider timeout or socket failure");
                }
                return emailServiceReturnValue.get();
            }
        };

        service = new ContactMessageService(repository, emailService, rateLimiter);
        service.setRecipientEmail("designtouchplus@gmail.com");
    }

    @Test
    @DisplayName("Successful submission saves entity, dispatches email to designtouchplus@gmail.com, and returns 201 response")
    void submitEnquiry_success() {
        ContactMessageRequest request = new ContactMessageRequest(
                "Alex Morgan",
                "alex@company.com",
                "Acme Studios",
                "Brand Video Editing",
                "We need 5 high-retention YouTube videos edited next month."
        );

        ContactMessageResponse response = service.submitEnquiry(request, "192.168.1.10");

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(201);
        assertThat(response.message()).contains("sent successfully");
        assertThat(response.id()).isNotNull();

        assertThat(savedEntity.get()).isNotNull();
        assertThat(savedEntity.get().getName()).isEqualTo("Alex Morgan");
        assertThat(savedEntity.get().getEmail()).isEqualTo("alex@company.com");
        assertThat(savedEntity.get().getCompany()).isEqualTo("Acme Studios");
        assertThat(savedEntity.get().getSubject()).isEqualTo("Brand Video Editing");
        assertThat(savedEntity.get().getMessage()).isEqualTo("We need 5 high-retention YouTube videos edited next month.");

        assertThat(emailServiceCalled.get()).isTrue();
        assertThat(capturedRecipient.get()).isEqualTo("designtouchplus@gmail.com");
        assertThat(capturedVisitorEmail.get()).isEqualTo("alex@company.com");
        assertThat(capturedSubject.get()).isEqualTo("Brand Video Editing");
    }

    @Test
    @DisplayName("When email provider fails or rejects email, EmailDeliveryException is thrown and success is not reported")
    void submitEnquiry_whenEmailProviderRejects_throwsEmailDeliveryException() {
        emailServiceReturnValue.set(false); // Provider rejected or failed

        ContactMessageRequest request = new ContactMessageRequest(
                "Sarah Connor",
                "sarah@resistance.org",
                null,
                "Urgent VFX",
                "Need motion graphics completed by Friday."
        );

        assertThatThrownBy(() -> service.submitEnquiry(request, "192.168.1.11"))
                .isInstanceOf(EmailDeliveryException.class)
                .hasMessageContaining("Unable to deliver your enquiry email via our email provider");

        assertThat(emailServiceCalled.get()).isTrue();
    }

    @Test
    @DisplayName("When email provider throws network exception, EmailDeliveryException is thrown")
    void submitEnquiry_whenEmailProviderThrows_throwsEmailDeliveryException() {
        emailServiceShouldThrow.set(true); // Network timeout

        ContactMessageRequest request = new ContactMessageRequest(
                "Bruce Wayne",
                "bruce@wayne.corp",
                "Wayne Enterprises",
                "Commercial",
                "Looking for a full production suite."
        );

        assertThatThrownBy(() -> service.submitEnquiry(request, "192.168.1.12"))
                .isInstanceOf(EmailDeliveryException.class)
                .hasMessageContaining("Unable to deliver your enquiry email");

        assertThat(emailServiceCalled.get()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Missing name throws IllegalArgumentException")
    void submitEnquiry_blankName_throwsIllegalArgument(String blankName) {
        ContactMessageRequest request = new ContactMessageRequest(
                blankName, "visitor@example.com", "Co", "Sub", "Hello"
        );

        assertThatThrownBy(() -> service.submitEnquiry(request, "192.168.1.13"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Name is required");

        assertThat(emailServiceCalled.get()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "user@", "@domain.com", "user@domain"})
    @DisplayName("Invalid email formats throw IllegalArgumentException")
    void submitEnquiry_invalidEmail_throwsIllegalArgument(String invalidEmail) {
        ContactMessageRequest request = new ContactMessageRequest(
                "John", invalidEmail, "Co", "Sub", "Hello"
        );

        assertThatThrownBy(() -> service.submitEnquiry(request, "192.168.1.14"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valid email");

        assertThat(emailServiceCalled.get()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Missing project description throws IllegalArgumentException")
    void submitEnquiry_blankMessage_throwsIllegalArgument(String blankMessage) {
        ContactMessageRequest request = new ContactMessageRequest(
                "John", "john@example.com", "Co", "Sub", blankMessage
        );

        assertThatThrownBy(() -> service.submitEnquiry(request, "192.168.1.15"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Project description is required");

        assertThat(emailServiceCalled.get()).isFalse();
    }

    @Test
    @DisplayName("Duplicate submission with identical email and message within 30s is rejected")
    void submitEnquiry_duplicateSubmission_rejected() {
        ContactMessageRequest request = new ContactMessageRequest(
                "Jane Doe",
                "jane@example.com",
                "Studio",
                "Short inquiry",
                "Exact same project description submitted twice rapidly."
        );

        // First attempt succeeds
        ContactMessageResponse firstResponse = service.submitEnquiry(request, "192.168.1.16");
        assertThat(firstResponse.status()).isEqualTo(201);

        // Immediate second attempt with same email + message is rejected
        assertThatThrownBy(() -> service.submitEnquiry(request, "192.168.1.16"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("A message with this content was recently received");
    }

    @Test
    @DisplayName("HTML escaping utility encodes all special characters to prevent HTML injection")
    void escapeHtml_properlyEncodesSpecialChars() {
        String unsafe = "<script>alert('XSS') & \"hack\"</script>";
        String safe = EmailService.escapeHtml(unsafe);

        assertThat(safe)
                .doesNotContain("<script>")
                .contains("&lt;script&gt;")
                .contains("&amp;")
                .contains("&quot;")
                .contains("&#39;");
    }
}

