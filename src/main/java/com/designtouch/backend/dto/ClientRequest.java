package com.designtouch.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record ClientRequest(
        @NotBlank String clientName,
        String logoUrl,
        String websiteUrl,
        Integer displayOrder,
        Boolean isActive,
        Boolean isFeatured) {
}
