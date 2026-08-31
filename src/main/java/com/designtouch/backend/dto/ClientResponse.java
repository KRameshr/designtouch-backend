package com.designtouch.backend.dto;

import com.designtouch.backend.entity.Client;
import java.util.UUID;

public record ClientResponse(UUID id, String clientName, String logoUrl, String websiteUrl) {

    public static ClientResponse from(Client c) {
        return new ClientResponse(c.getId(), c.getClientName(), c.getLogoUrl(), c.getWebsiteUrl());
    }
}
