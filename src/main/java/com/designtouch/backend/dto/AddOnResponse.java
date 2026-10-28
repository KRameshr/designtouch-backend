package com.designtouch.backend.dto;

import com.designtouch.backend.entity.AddOn;

import java.util.UUID;

public record AddOnResponse(
        UUID id,
        String name,
        String icon,
        String priceLabel,
        Integer displayOrder,
        Boolean isActive,
        String customIconUrl,
        String customIconPublicId
) {
    public static AddOnResponse from(AddOn a) {
        if (a == null) return null;
        return new AddOnResponse(
                a.getId(),
                a.getName(),
                a.getIcon(),
                a.getPriceLabel(),
                a.getDisplayOrder(),
                a.getIsActive() != null ? a.getIsActive() : true,
                a.getCustomIconUrl(),
                a.getCustomIconPublicId()
        );
    }
}
