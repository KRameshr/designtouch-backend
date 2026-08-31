package com.designtouch.backend.dto;

import com.designtouch.backend.entity.Faq;
import java.util.UUID;

public record FaqResponse(UUID id, String question, String answer) {

    public static FaqResponse from(Faq f) {
        return new FaqResponse(f.getId(), f.getQuestion(), f.getAnswer());
    }
}
