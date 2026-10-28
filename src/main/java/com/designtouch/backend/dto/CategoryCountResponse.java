package com.designtouch.backend.dto;

// Blog sidebar "CATEGORIES" list — e.g. { category: "VIDEO_EDITING", name: "Video Editing", count: 12 }
public record CategoryCountResponse(String category, String name, Long count) {
}
