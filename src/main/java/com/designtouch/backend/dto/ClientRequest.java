package com.designtouch.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record ClientRequest(
        @NotBlank String clientName,
        @NotBlank String logoUrl,
        String websiteUrl,
        Integer displayOrder) {
}
