package com.designtouch.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record TestimonialRequest(
        @NotBlank String quote,
        @NotBlank String authorName,
        String authorRole,
        String authorCompany,
        String authorAvatarUrl,
        Short rating,
        Integer displayOrder,
        Boolean isActive) {
}
