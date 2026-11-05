package com.designtouch.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ChatLeadResponse(
        UUID id,
        String name,
        String email,
        String conversationStatus,
        OffsetDateTime createdAt
) {}

