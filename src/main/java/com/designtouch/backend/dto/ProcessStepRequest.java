package com.designtouch.backend.dto;

import com.designtouch.backend.entity.enums.PageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProcessStepRequest(
        PageType pageType,
        @NotNull Short stepNumber,
        @NotBlank String title,
        String description,
        String icon,
        Integer displayOrder,
        Boolean isActive
) {
}

