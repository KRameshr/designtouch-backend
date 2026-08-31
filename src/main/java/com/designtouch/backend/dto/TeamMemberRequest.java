package com.designtouch.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record TeamMemberRequest(
        @NotBlank String name,
        @NotBlank String role,
        String photoUrl,
        String linkedinUrl,
        String instagramUrl,
        Integer displayOrder,
        Boolean isActive) {
}
