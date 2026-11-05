package com.designtouch.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ContactMessageResponse(
        String message,
        int status,
        UUID id,
        OffsetDateTime timestamp
) {
    public static ContactMessageResponse of(String message, UUID id) {
        return new ContactMessageResponse(message, 201, id, OffsetDateTime.now());
    }
}

