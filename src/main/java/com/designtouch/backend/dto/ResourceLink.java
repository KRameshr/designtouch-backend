package com.designtouch.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record ResourceLink(
        @NotBlank String title,
        @NotBlank String url,
        String description
) {}

