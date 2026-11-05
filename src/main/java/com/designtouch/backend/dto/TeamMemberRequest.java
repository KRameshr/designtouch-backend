package com.designtouch.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public record TeamMemberRequest(
        @NotBlank String name,
        @NotBlank String role,
        @JsonAlias("photo_url") String photoUrl,
        @JsonAlias("linkedin_url") String linkedinUrl,
        @JsonAlias("instagram_url") String instagramUrl,
        @JsonAlias("display_order") Integer displayOrder,
        @JsonAlias("is_active") Boolean isActive) {
}
