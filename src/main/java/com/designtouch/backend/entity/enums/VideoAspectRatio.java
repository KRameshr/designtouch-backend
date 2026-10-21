package com.designtouch.backend.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum VideoAspectRatio {
    RATIO_9_16("9:16"),
    RATIO_16_9("16:9"),
    RATIO_1_1("1:1"),
    RATIO_4_5("4:5"),
    AUTO("AUTO");

    private final String value;

    VideoAspectRatio(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static VideoAspectRatio fromString(String text) {
        if (text == null || text.isBlank()) {
            return AUTO;
        }
        String trimmed = text.trim();
        for (VideoAspectRatio ratio : VideoAspectRatio.values()) {
            if (ratio.value.equalsIgnoreCase(trimmed) || ratio.name().equalsIgnoreCase(trimmed)) {
                return ratio;
            }
        }
        String normalized = trimmed.toUpperCase().replace("-", "_").replace(" ", "_");
        if (normalized.equals("9/16") || normalized.equals("9:16") || normalized.equals("RATIO_9_16") || normalized.equals("NINE_SIXTEEN")) {
            return RATIO_9_16;
        }
        if (normalized.equals("16/9") || normalized.equals("16:9") || normalized.equals("RATIO_16_9") || normalized.equals("SIXTEEN_NINE")) {
            return RATIO_16_9;
        }
        if (normalized.equals("1/1") || normalized.equals("1:1") || normalized.equals("RATIO_1_1") || normalized.equals("ONE_ONE") || normalized.equals("SQUARE")) {
            return RATIO_1_1;
        }
        if (normalized.equals("4/5") || normalized.equals("4:5") || normalized.equals("RATIO_4_5") || normalized.equals("FOUR_FIVE")) {
            return RATIO_4_5;
        }
        if (normalized.equals("AUTO") || normalized.equals("ORIGINAL") || normalized.equals("AUTO_DETECT")) {
            return AUTO;
        }
        throw new IllegalArgumentException("Unknown video aspect ratio: '" + text + "'. Allowed values: 9:16, 16:9, 1:1, 4:5, AUTO");
    }
}

