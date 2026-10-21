package com.designtouch.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record ToolRequest(
        @NotBlank String name,
        String iconKey,
        String logoUrl,
        String websiteUrl,
        String role,
        Integer displayOrder,
        Boolean isActive
) {
}

