package com.designtouch.backend.dto;

import com.designtouch.backend.entity.Tool;

import java.util.UUID;

public record ToolResponse(
        UUID id,
        String name,
        String iconKey,
        String logoUrl,
        String websiteUrl,
        String role,
        Integer displayOrder,
        Boolean isActive
) {
    public static ToolResponse from(Tool t) {
        return new ToolResponse(
                t.getId(),
                t.getName(),
                t.getIconKey(),
                t.getLogoUrl(),
                t.getWebsiteUrl(),
                t.getRole(),
                t.getDisplayOrder(),
                t.getIsActive() != null ? t.getIsActive() : true
        );
    }
}
