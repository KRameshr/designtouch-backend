package com.designtouch.backend.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CategoryArticleMoveRequest(
        @NotNull(message = "Destination category ID is required")
        UUID destinationCategoryId
) {
}

