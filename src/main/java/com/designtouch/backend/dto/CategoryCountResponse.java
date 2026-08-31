package com.designtouch.backend.dto;

import com.designtouch.backend.entity.enums.BlogCategory;

// Blog sidebar "CATEGORIES" list — e.g. { "Video Editing", 12 }
public record CategoryCountResponse(BlogCategory category, Long count) {
}
