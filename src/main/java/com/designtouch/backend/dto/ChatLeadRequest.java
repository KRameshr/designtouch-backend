package com.designtouch.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatLeadRequest(
        @NotBlank(message = "Please provide your full name")
        @Size(max = 150, message = "Name must not exceed 150 characters")
        String name,

        @NotBlank(message = "Please provide your email address")
        @Email(message = "Please enter a valid email address")
        @Size(max = 150, message = "Email must not exceed 150 characters")
        String email
) {}

