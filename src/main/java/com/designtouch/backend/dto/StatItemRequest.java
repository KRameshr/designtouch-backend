package com.designtouch.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record StatItemRequest(
        @NotBlank String label,
        @NotBlank String value,
        String suffix,
        String description,
        String icon,
        Integer displayOrder) {
}

