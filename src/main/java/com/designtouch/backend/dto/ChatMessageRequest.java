package com.designtouch.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ChatMessageRequest(
        UUID conversationId,
        UUID leadId,

        @NotBlank(message = "Message content cannot be empty")
        @Size(max = 2000, message = "Message content must not exceed 2000 characters")
        String content
) {
    public ChatMessageRequest(UUID conversationId, String content) {
        this(conversationId, null, content);
    }
}
