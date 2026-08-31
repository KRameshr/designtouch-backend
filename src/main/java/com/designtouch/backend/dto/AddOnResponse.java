package com.designtouch.backend.dto;

import com.designtouch.backend.entity.AddOn;
import java.util.UUID;

public record AddOnResponse(UUID id, String name, String icon, String priceLabel) {

    public static AddOnResponse from(AddOn a) {
        return new AddOnResponse(a.getId(), a.getName(), a.getIcon(), a.getPriceLabel());
    }
}
