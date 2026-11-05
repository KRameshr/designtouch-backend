package com.designtouch.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record BulkDeleteRequest(
        @NotEmpty(message = "Please provide at least one ID to delete")
        List<UUID> ids
) {}

