package com.designtouch.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record CompanyValueRequest(
        @NotBlank String title,
        String description,
        String icon,
        Integer displayOrder) {
}

