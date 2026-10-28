package com.designtouch.backend.controller;

import com.designtouch.backend.dto.NewsletterResponse;
import com.designtouch.backend.exception.DuplicateSubscriptionException;
import com.designtouch.backend.exception.GlobalExceptionHandler;
import com.designtouch.backend.service.NewsletterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Proxy;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NewsletterSubscriberControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private NewsletterService newsletterService;
    private boolean duplicateThrows = false;
    private boolean rateLimitThrows = false;
    private boolean retryResponse = false;

    @BeforeEach
    void setUp() {
        duplicateThrows = false;
        rateLimitThrows = false;
        retryResponse = false;

        // Dynamic proxy for NewsletterService so MockMvc standalone tests can run without Spring Boot context
        newsletterService = new NewsletterService(null, null, null) {
            @Override
            public NewsletterResponse subscribe(String rawEmail, String clientIp) {
                if (duplicateThrows) {
                    throw new DuplicateSubscriptionException("This email is already subscribed to our newsletter.");
                }
                if (rateLimitThrows) {
                    throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many subscription attempts.");
                }
                if (rawEmail == null || rawEmail.isBlank()) {
                    throw new IllegalArgumentException("Email is required.");
                }
                if (!rawEmail.contains("@")) {
                    throw new IllegalArgumentException("Please enter a valid email address.");
                }
                if (retryResponse) {
                    return NewsletterResponse.of("Confirmation email sent successfully.", 200, rawEmail.trim().toLowerCase(), true);
                }
                return NewsletterResponse.of("Successfully subscribed to the newsletter.", 201, rawEmail.trim().toLowerCase(), true);
            }
        };

        NewsletterSubscriberController controller = new NewsletterSubscriberController(newsletterService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/newsletter/subscribe returns 201 Created for valid email")
    void subscribe_withValidEmail_returns201Created() throws Exception {
        Map<String, String> body = Map.of("email", "videocreator@designtouchplus.com");

        mockMvc.perform(post("/api/newsletter/subscribe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.email").value("videocreator@designtouchplus.com"))
                .andExpect(jsonPath("$.message").value(containsString("Successfully subscribed")));
    }

    @Test
    @DisplayName("POST /api/newsletter/subscribe returns 200 OK for safe retry on existing unconfirmed email")
    void subscribe_withExistingUnconfirmedEmail_returns200OkOnRetry() throws Exception {
        retryResponse = true;
        Map<String, String> body = Map.of("email", "kramesh9652@gmail.com");

        mockMvc.perform(post("/api/newsletter/subscribe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.email").value("kramesh9652@gmail.com"))
                .andExpect(jsonPath("$.emailSent").value(true))
                .andExpect(jsonPath("$.message").value(containsString("Confirmation email sent")));
    }

    @Test
    @DisplayName("POST /api/newsletter/subscribe returns 409 Conflict for duplicate subscription")
    void subscribe_withDuplicateEmail_returns409Conflict() throws Exception {
        duplicateThrows = true;
        Map<String, String> body = Map.of("email", "existing@designtouchplus.com");

        mockMvc.perform(post("/api/newsletter/subscribe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(containsString("already subscribed")));
    }

    @Test
    @DisplayName("POST /api/newsletter/subscribe returns 400 Bad Request for invalid email")
    void subscribe_withInvalidEmail_returns400BadRequest() throws Exception {
        Map<String, String> body = Map.of("email", "not-an-email");

        mockMvc.perform(post("/api/newsletter/subscribe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/newsletter/subscribe returns 400 Bad Request for blank email")
    void subscribe_withBlankEmail_returns400BadRequest() throws Exception {
        Map<String, String> body = Map.of("email", "");

        mockMvc.perform(post("/api/newsletter/subscribe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/newsletter/subscribe returns 429 Too Many Requests when rate limit exceeded")
    void subscribe_whenRateLimitExceeded_returns429TooManyRequests() throws Exception {
        rateLimitThrows = true;
        Map<String, String> body = Map.of("email", "spammer@example.com");

        mockMvc.perform(post("/api/newsletter/subscribe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429));
    }
}

