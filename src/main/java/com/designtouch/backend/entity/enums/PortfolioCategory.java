package com.designtouch.backend.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PortfolioCategory {
    YOUTUBE,
    INSTAGRAM_REELS,
    COMMERCIAL,
    MOTION_GRAPHICS,
    CORPORATE,
    REAL_ESTATE;

    @JsonCreator
    public static PortfolioCategory fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return PortfolioCategory.valueOf(value.trim().toUpperCase().replace('-', '_'));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
