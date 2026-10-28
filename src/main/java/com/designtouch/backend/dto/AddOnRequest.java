package com.designtouch.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddOnRequest(
        @NotBlank @Size(max = 100)
        String name,

        @Size(max = 100)
        String icon,

        @NotBlank @Size(max = 50)
        String priceLabel,

        Integer displayOrder,

        Boolean isActive,

        String customIconUrl,

        String customIconPublicId
) {
}

