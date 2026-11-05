package com.designtouch.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminChatLeadDetailResponse(
        UUID id,
        String name,
        String email,
        String conversationStatus,
        String source,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
