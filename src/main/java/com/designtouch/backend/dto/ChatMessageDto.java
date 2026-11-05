package com.designtouch.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ChatMessageDto(
        UUID id,
        String senderType,
        String content,
        OffsetDateTime createdAt
) {}

