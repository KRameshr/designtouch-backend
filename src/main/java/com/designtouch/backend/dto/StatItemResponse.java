package com.designtouch.backend.dto;

import com.designtouch.backend.entity.StatItem;
import java.util.UUID;

public record StatItemResponse(
        UUID id,
        String label,
        String value,
        String suffix,
        String description,
        String icon,
        Integer displayOrder) {

    public static StatItemResponse from(StatItem s) {
        return new StatItemResponse(
                s.getId(),
                s.getLabel(),
                s.getValue(),
                s.getSuffix(),
                s.getDescription() != null ? s.getDescription() : s.getLabel(),
                s.getIcon(),
                s.getDisplayOrder());
    }
}
