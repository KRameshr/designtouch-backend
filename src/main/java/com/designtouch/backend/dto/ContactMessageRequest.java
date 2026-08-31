package com.designtouch.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Request body for POST /api/contact — validated before it ever reaches the service
public record ContactMessageRequest(
        @NotBlank(message = "Name is required") @Size(max = 150) String name,
        @NotBlank(message = "Email is required") @Email(message = "Enter a valid email") String email,
        @Size(max = 150) String company,
        @Size(max = 250) String subject,
        @NotBlank(message = "Message is required") String message) {
}
