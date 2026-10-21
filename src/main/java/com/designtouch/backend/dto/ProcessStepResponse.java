package com.designtouch.backend.dto;

import com.designtouch.backend.entity.ProcessStep;
import com.designtouch.backend.entity.enums.PageType;

import java.util.UUID;

public record ProcessStepResponse(
        UUID id,
        PageType pageType,
        Short stepNumber,
        String title,
        String description,
        String icon,
        Integer displayOrder,
        Boolean isActive
) {
    public static ProcessStepResponse from(ProcessStep p) {
        return new ProcessStepResponse(
                p.getId(),
                p.getPageType(),
                p.getStepNumber(),
                p.getTitle(),
                p.getDescription(),
                p.getIcon(),
                p.getDisplayOrder(),
                p.getIsActive() != null ? p.getIsActive() : true
        );
    }
}
