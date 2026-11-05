package com.designtouch.backend.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ChatConversationResponse(
        UUID id,
        UUID leadId,
        String leadName,
        String status,
        OffsetDateTime createdAt,
        List<ChatMessageDto> messages
) {}

