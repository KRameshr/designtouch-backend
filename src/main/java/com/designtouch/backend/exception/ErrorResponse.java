package com.designtouch.backend.exception;

import java.time.OffsetDateTime;

public record ErrorResponse(String message, int status, OffsetDateTime timestamp) {

    public static ErrorResponse of(String message, int status) {
        return new ErrorResponse(message, status, OffsetDateTime.now());
    }
}
