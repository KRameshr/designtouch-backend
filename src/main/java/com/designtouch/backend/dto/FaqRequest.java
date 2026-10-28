package com.designtouch.backend.dto;

import com.designtouch.backend.entity.enums.FaqCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FaqRequest(
        @NotBlank @Size(max = 300)
        String question,

        @NotBlank
        String answer,

        @NotNull
        FaqCategory category,

        Integer displayOrder,

        Boolean isActive
) {
}

