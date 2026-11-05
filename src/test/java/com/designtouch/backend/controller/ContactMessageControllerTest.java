package com.designtouch.backend.controller;

import com.designtouch.backend.dto.ContactMessageRequest;
import com.designtouch.backend.dto.ContactMessageResponse;
import com.designtouch.backend.exception.EmailDeliveryException;
import com.designtouch.backend.exception.GlobalExceptionHandler;
import com.designtouch.backend.service.ContactMessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ContactMessageControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private ContactMessageService contactMessageService;
    private AtomicBoolean providerFails;
    private AtomicBoolean rateLimitFails;

    @BeforeEach
    void setUp() {
        providerFails = new AtomicBoolean(false);
        rateLimitFails = new AtomicBoolean(false);

        // Subclass ContactMessageService so MockMvc standalone tests can run fast without database
        contactMessageService = new ContactMessageService(null, null, null) {
            @Override
            public ContactMessageResponse submitEnquiry(ContactMessageRequest request, String clientIp) {
                if (providerFails.get()) {
                    throw new EmailDeliveryException("Unable to deliver your enquiry email via our email provider. Please try again or reach out to us directly.");
                }
                if (rateLimitFails.get()) {
                    throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many enquiry submissions. Please wait a minute.");
                }
                if (request.name() == null || request.name().isBlank()) {
                    throw new IllegalArgumentException("Name is required.");
                }
                if (request.email() == null || request.email().isBlank() || !request.email().contains("@")) {
                    throw new IllegalArgumentException("Please enter a valid email address.");
                }
                if (request.message() == null || request.message().isBlank()) {
                    throw new IllegalArgumentException("Project description is required.");
                }

                return ContactMessageResponse.of(
                        "Thank you! Your message has been sent successfully. We will review your project inquiry and reply within 24 hours.",
                        UUID.fromString("11111111-2222-3333-4444-555555555555")
                );
            }
        };

        ContactMessageController controller = new ContactMessageController(contactMessageService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/contact with valid data returns 201 Created and JSON confirmation")
    void submitContact_validData_returns201Created() throws Exception {
        ContactMessageRequest request = new ContactMessageRequest(
                "Alex Morgan",
                "alex@designco.com",
                "DesignCo",
                "Video Project",
                "We need full motion graphics for our new campaign."
        );

        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value(containsString("sent successfully")))
                .andExpect(jsonPath("$.id").value("11111111-2222-3333-4444-555555555555"));
    }

    @Test
    @DisplayName("POST /api/contact when email provider fails returns 502 Bad Gateway with error JSON")
    void submitContact_whenEmailProviderFails_returns502BadGateway() throws Exception {
        providerFails.set(true);

        ContactMessageRequest request = new ContactMessageRequest(
                "David Hassel",
                "david@knight.org",
                null,
                "Support",
                "Interested in package pricing."
        );

        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value(containsString("Unable to deliver your enquiry email")));
    }

    @Test
    @DisplayName("POST /api/contact with invalid email returns 400 Bad Request with field error")
    void submitContact_invalidEmail_returns400BadRequest() throws Exception {
        ContactMessageRequest request = new ContactMessageRequest(
                "David",
                "not-an-email",
                null,
                null,
                "Description here"
        );

        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value(containsString("valid email")));
    }

    @Test
    @DisplayName("POST /api/contact with missing name returns 400 Bad Request with field error")
    void submitContact_missingName_returns400BadRequest() throws Exception {
        ContactMessageRequest request = new ContactMessageRequest(
                "",
                "valid@example.com",
                null,
                null,
                "Description here"
        );

        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value(containsString("Name is required")));
    }

    @Test
    @DisplayName("POST /api/contact when rate limited returns 429 Too Many Requests")
    void submitContact_whenRateLimited_returns429TooManyRequests() throws Exception {
        rateLimitFails.set(true);

        ContactMessageRequest request = new ContactMessageRequest(
                "Spammer",
                "spam@example.com",
                null,
                null,
                "Repeated message"
        );

        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.message").value(containsString("Too many enquiry submissions")));
    }
}

