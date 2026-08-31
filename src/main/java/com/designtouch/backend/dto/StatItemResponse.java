package com.designtouch.backend.dto;

import com.designtouch.backend.entity.StatItem;
import java.util.UUID;

public record StatItemResponse(UUID id, String label, Integer value, String suffix, String description, String icon) {

    public static StatItemResponse from(StatItem s) {
        return new StatItemResponse(s.getId(), s.getLabel(), s.getValue(), s.getSuffix(), s.getDescription(), s.getIcon());
    }
}
