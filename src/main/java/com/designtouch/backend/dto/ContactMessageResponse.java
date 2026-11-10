package com.designtouch.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ContactMessageResponse(
        String message,
        int status,
        UUID id,
        OffsetDateTime timestamp,
        boolean received,
        boolean notificationEmailSent
) {
    public static ContactMessageResponse of(String message, UUID id) {
        return of(message, id, true, true);
    }

    public static ContactMessageResponse of(String message, UUID id, boolean received, boolean notificationEmailSent) {
        return new ContactMessageResponse(message, 201, id, OffsetDateTime.now(), received, notificationEmailSent);
    }
}

