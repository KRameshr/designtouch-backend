package com.designtouch.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateSubscriptionException extends RuntimeException {

    public DuplicateSubscriptionException(String message) {
        super(message);
    }
}

