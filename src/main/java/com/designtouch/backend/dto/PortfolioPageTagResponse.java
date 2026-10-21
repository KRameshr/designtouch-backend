package com.designtouch.backend.dto;

import com.designtouch.backend.entity.PortfolioPageTag;

import java.util.UUID;

public record PortfolioPageTagResponse(
        UUID id,
        String label,
        Integer displayOrder,
        Boolean isActive) {

    public static PortfolioPageTagResponse from(PortfolioPageTag entity) {
        if (entity == null) return null;
        return new PortfolioPageTagResponse(
                entity.getId(),
                entity.getLabel(),
                entity.getDisplayOrder(),
                entity.getIsActive()
        );
    }
}

