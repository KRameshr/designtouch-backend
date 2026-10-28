package com.designtouch.backend.dto;

import com.designtouch.backend.entity.Faq;
import com.designtouch.backend.entity.enums.FaqCategory;

import java.util.UUID;

public record FaqResponse(
        UUID id,
        String question,
        String answer,
        FaqCategory category,
        Integer displayOrder,
        Boolean isActive
) {
    public static FaqResponse from(Faq f) {
        if (f == null) return null;
        return new FaqResponse(
                f.getId(),
                f.getQuestion(),
                f.getAnswer(),
                f.getCategory(),
                f.getDisplayOrder(),
                f.getIsActive() != null ? f.getIsActive() : true
        );
    }
}
