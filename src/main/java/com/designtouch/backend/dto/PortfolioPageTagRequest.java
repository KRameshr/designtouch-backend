package com.designtouch.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record PortfolioPageTagRequest(
        @NotBlank String label,
        Integer displayOrder,
        Boolean isActive) {
}

