package com.designtouch.backend.dto;

import com.designtouch.backend.entity.enums.ContentType;
import com.designtouch.backend.entity.enums.PortfolioCategory;
import com.designtouch.backend.entity.enums.VideoAspectRatio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

// Request body for admin create/update — no id/createdAt/viewCount (server-controlled)
public record PortfolioItemRequest(
        @NotBlank String title,
        @NotNull ContentType contentType,
        PortfolioCategory category,
        String subCategory,
        String description,
        String duration,
        String videoUrl,
        String imageUrl,
        String thumbnailUrl,
        String videoPublicId,
        Integer displayOrder,
        Boolean isFeatured,
        UUID categoryId,
        String imagePublicId,
        String slug,
        String liveVideoUrl,
        String googleDriveUrl,
        List<ResourceLink> resourceLinks,
        VideoAspectRatio videoAspectRatio) {

    // Constructor with all fields prior to videoAspectRatio
    public PortfolioItemRequest(
            String title,
            ContentType contentType,
            PortfolioCategory category,
            String subCategory,
            String description,
            String duration,
            String videoUrl,
            String imageUrl,
            String thumbnailUrl,
            String videoPublicId,
            Integer displayOrder,
            Boolean isFeatured,
            UUID categoryId,
            String imagePublicId,
            String slug,
            String liveVideoUrl,
            String googleDriveUrl,
            List<ResourceLink> resourceLinks) {
        this(title, contentType, category, subCategory, description, duration, videoUrl, imageUrl, thumbnailUrl,
                videoPublicId, displayOrder, isFeatured, categoryId, imagePublicId, slug, liveVideoUrl, googleDriveUrl, resourceLinks, null);
    }

    // Constructor with all fields prior to delivery links
    public PortfolioItemRequest(
            String title,
            ContentType contentType,
            PortfolioCategory category,
            String subCategory,
            String description,
            String duration,
            String videoUrl,
            String imageUrl,
            String thumbnailUrl,
            String videoPublicId,
            Integer displayOrder,
            Boolean isFeatured,
            UUID categoryId,
            String imagePublicId) {
        this(title, contentType, category, subCategory, description, duration, videoUrl, imageUrl, thumbnailUrl,
                videoPublicId, displayOrder, isFeatured, categoryId, imagePublicId, null, null, null, null);
    }

    // Backward-compatible constructor with categoryId but without imagePublicId
    public PortfolioItemRequest(
            String title,
            ContentType contentType,
            PortfolioCategory category,
            String subCategory,
            String description,
            String duration,
            String videoUrl,
            String imageUrl,
            String thumbnailUrl,
            String videoPublicId,
            Integer displayOrder,
            Boolean isFeatured,
            UUID categoryId) {
        this(title, contentType, category, subCategory, description, duration, videoUrl, imageUrl, thumbnailUrl,
                videoPublicId, displayOrder, isFeatured, categoryId, null, null, null, null, null);
    }

    // Backward-compatible constructor for existing tests and callers without categoryId and imagePublicId
    public PortfolioItemRequest(
            String title,
            ContentType contentType,
            PortfolioCategory category,
            String subCategory,
            String description,
            String duration,
            String videoUrl,
            String imageUrl,
            String thumbnailUrl,
            String videoPublicId,
            Integer displayOrder,
            Boolean isFeatured) {
        this(title, contentType, category, subCategory, description, duration, videoUrl, imageUrl, thumbnailUrl,
                videoPublicId, displayOrder, isFeatured, null, null, null, null, null, null);
    }
}
