package com.designtouch.backend.controller;

import com.designtouch.backend.dto.NewsletterResponse;
import com.designtouch.backend.dto.NewsletterSubscribeRequest;
import com.designtouch.backend.service.NewsletterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/newsletter")
@RequiredArgsConstructor
public class NewsletterSubscriberController {

    private final NewsletterService newsletterService;

    /**
     * Public newsletter subscription endpoint for blog sidebar and footer.
     * Enforces email format validation, normalization, duplicate prevention,
     * rate limiting, database persistence, and welcome email delivery.
     *
     * Responses:
     * - 201 Created: New subscription successfully registered
     * - 409 Conflict: Email already subscribed
     * - 400 Bad Request: Missing or invalid email address
     * - 429 Too Many Requests: Rate limit exceeded
     * - 500 Internal Server Error: Database or system error (never leaking internals)
     */
    @PostMapping("/subscribe")
    public ResponseEntity<NewsletterResponse> subscribe(
            @Valid @RequestBody NewsletterSubscribeRequest request,
            HttpServletRequest httpRequest
    ) {
        String clientIp = extractClientIp(httpRequest);
        NewsletterResponse response = newsletterService.subscribe(request.email(), clientIp);
        return ResponseEntity.status(response.status()).body(response);
    }

    private String extractClientIp(HttpServletRequest request) {
        if (request == null) {
            return "127.0.0.1";
        }
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
