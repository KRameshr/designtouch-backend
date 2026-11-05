package com.designtouch.backend.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ChatConversationRequest(
        @NotNull(message = "Lead ID is required")
        UUID leadId,

        String title
) {}

