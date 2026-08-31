package com.designtouch.backend.dto;

import com.designtouch.backend.entity.CompanyValue;
import java.util.UUID;

public record CompanyValueResponse(UUID id, String title, String description, String icon) {

    public static CompanyValueResponse from(CompanyValue v) {
        return new CompanyValueResponse(v.getId(), v.getTitle(), v.getDescription(), v.getIcon());
    }
}
