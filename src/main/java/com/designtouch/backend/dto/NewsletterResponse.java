package com.designtouch.backend.dto;

import java.time.OffsetDateTime;

public record NewsletterResponse(
        String message,
        int status,
        String email,
        boolean emailSent,
        OffsetDateTime timestamp
) {
    public static NewsletterResponse of(String message, int status, String email, boolean emailSent) {
        return new NewsletterResponse(message, status, email, emailSent, OffsetDateTime.now());
    }
}

