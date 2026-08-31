package com.designtouch.backend.dto;

import com.designtouch.backend.entity.Service;

import java.util.List;
import java.util.UUID;

public record ServiceResponse(
        UUID id, String icon, String title, String description,
        List<String> features, String colorTheme, String slug, String orderNumber) {

    public static ServiceResponse from(Service s) {
        return new ServiceResponse(
                s.getId(), s.getIcon(), s.getTitle(), s.getDescription(),
                s.getFeatures(), s.getColorTheme(), s.getSlug(), s.getOrderNumber());
    }
}
