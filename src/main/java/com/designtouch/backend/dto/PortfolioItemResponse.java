package com.designtouch.backend.dto;

import com.designtouch.backend.entity.PortfolioItem;
import com.designtouch.backend.entity.enums.ContentType;
import com.designtouch.backend.entity.enums.PortfolioCategory;

import java.util.UUID;

public record PortfolioItemResponse(
        UUID id, String title, ContentType contentType, PortfolioCategory category,
        String subCategory, String description, String duration,
        String videoUrl, String imageUrl, String thumbnailUrl, Integer viewCount) {

    public static PortfolioItemResponse from(PortfolioItem p) {
        return new PortfolioItemResponse(
                p.getId(), p.getTitle(), p.getContentType(), p.getCategory(),
                p.getSubCategory(), p.getDescription(), p.getDuration(),
                p.getVideoUrl(), p.getImageUrl(), p.getThumbnailUrl(), p.getViewCount());
    }
}
