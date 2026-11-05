package com.designtouch.backend.dto;

import com.designtouch.backend.entity.Client;
import java.util.UUID;

public record ClientResponse(
        UUID id,
        String clientName,
        String logoUrl,
        String websiteUrl,
        Integer displayOrder,
        Boolean isActive,
        Boolean isFeatured) {

    public static ClientResponse from(Client c) {
        return new ClientResponse(
                c.getId(),
                c.getClientName(),
                c.getLogoUrl(),
                c.getWebsiteUrl(),
                c.getDisplayOrder() != null ? c.getDisplayOrder() : 0,
                c.getIsActive() != null ? c.getIsActive() : true,
                c.getIsFeatured() != null ? c.getIsFeatured() : true
        );
    }
}
