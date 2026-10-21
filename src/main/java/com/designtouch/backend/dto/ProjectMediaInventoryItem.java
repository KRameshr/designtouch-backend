package com.designtouch.backend.dto;

import com.designtouch.backend.entity.enums.ContentType;
import java.util.UUID;

public record ProjectMediaInventoryItem(
        UUID projectId,
        String projectTitle,
        ContentType contentType,
        String categoryName,
        String mediaField,
        String currentUrl,
        String currentPublicId,
        String sourceType,
        String status,
        String newUrl,
        String newPublicId,
        String notes
) {}

