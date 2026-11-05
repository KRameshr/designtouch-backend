package com.designtouch.backend.dto;

import com.designtouch.backend.entity.Testimonial;
import java.util.UUID;

public record TestimonialResponse(
        UUID id,
        String quote,
        String authorName,
        String authorRole,
        String authorCompany,
        String authorAvatarUrl,
        Short rating,
        Integer displayOrder,
        Boolean isActive,
        String serviceType) {

    public static TestimonialResponse from(Testimonial t) {
        String effectiveServiceType = (t.getServiceType() != null && !t.getServiceType().isBlank())
                ? t.getServiceType()
                : "General Feedback";
        return new TestimonialResponse(
                t.getId(),
                t.getQuote(),
                t.getAuthorName(),
                t.getAuthorRole(),
                t.getAuthorCompany(),
                t.getAuthorAvatarUrl(),
                t.getRating(),
                t.getDisplayOrder(),
                t.getIsActive(),
                effectiveServiceType);
    }
}
