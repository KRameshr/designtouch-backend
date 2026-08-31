package com.designtouch.backend.dto;

import com.designtouch.backend.entity.Testimonial;
import java.util.UUID;

public record TestimonialResponse(
        UUID id, String quote, String authorName, String authorRole,
        String authorCompany, String authorAvatarUrl, Short rating) {

    public static TestimonialResponse from(Testimonial t) {
        return new TestimonialResponse(
                t.getId(), t.getQuote(), t.getAuthorName(), t.getAuthorRole(),
                t.getAuthorCompany(), t.getAuthorAvatarUrl(), t.getRating());
    }
}
