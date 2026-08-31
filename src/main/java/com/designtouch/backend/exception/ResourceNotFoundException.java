package com.designtouch.backend.exception;

/**
 * Thrown when a requested entity (by id, slug, etc.) doesn't exist.
 * Handled globally by GlobalExceptionHandler, which turns it into a 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
