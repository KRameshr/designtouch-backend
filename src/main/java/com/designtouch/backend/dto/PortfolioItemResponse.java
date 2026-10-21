package com.designtouch.backend.dto;

import com.designtouch.backend.entity.PortfolioItem;
import com.designtouch.backend.entity.enums.CategoryType;
import com.designtouch.backend.entity.enums.ContentType;
import com.designtouch.backend.entity.enums.PortfolioCategory;
import com.designtouch.backend.entity.enums.VideoAspectRatio;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record PortfolioItemResponse(
        UUID id,
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
        Integer viewCount,
        Integer displayOrder,
        Boolean isFeatured,
        UUID categoryId,
        String categoryName,
        String categorySlug,
        CategoryType categoryType,
        String imagePublicId,
        String slug,
        String liveVideoUrl,
        String googleDriveUrl,
        List<ResourceLink> resourceLinks,
        OffsetDateTime createdAt,
        VideoAspectRatio videoAspectRatio) {

    public static PortfolioItemResponse from(PortfolioItem p) {
        UUID catId = null;
        String catName = null;
        String catSlug = null;
        CategoryType catType = null;

        if (p.getCategoryRef() != null) {
            catId = p.getCategoryRef().getId();
            catName = p.getCategoryRef().getName();
            catSlug = p.getCategoryRef().getSlug();
            catType = p.getCategoryRef().getCategoryType();
        } else if (p.getCategory() != null) {
            catSlug = p.getCategory().name().toLowerCase().replace('_', '-');
            catName = formatLegacyCategory(p.getCategory());
            catType = CategoryType.VIDEO_EDITING;
        } else if (p.getSubCategory() != null && !p.getSubCategory().isBlank()) {
            catName = p.getSubCategory();
            catSlug = p.getSubCategory().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
            catType = CategoryType.THUMBNAIL_DESIGN;
        }

        VideoAspectRatio ratio = p.getVideoAspectRatio();
        if (p.getContentType() == ContentType.VIDEO_EDITING && ratio == null) {
            ratio = VideoAspectRatio.AUTO;
        } else if (p.getContentType() == ContentType.THUMBNAIL_DESIGN) {
            ratio = null;
        }

        return new PortfolioItemResponse(
                p.getId(),
                p.getTitle(),
                p.getContentType(),
                p.getCategory(),
                p.getSubCategory(),
                p.getDescription(),
                p.getDuration(),
                p.getVideoUrl(),
                p.getImageUrl(),
                p.getThumbnailUrl(),
                p.getVideoPublicId(),
                p.getViewCount(),
                p.getDisplayOrder(),
                p.getIsFeatured(),
                catId,
                catName,
                catSlug,
                catType,
                p.getImagePublicId(),
                p.getSlug(),
                p.getLiveVideoUrl(),
                p.getGoogleDriveUrl(),
                p.getResourceLinks() != null ? p.getResourceLinks() : List.of(),
                p.getCreatedAt(),
                ratio
        );
    }

    private static String formatLegacyCategory(PortfolioCategory category) {
        return switch (category) {
            case YOUTUBE -> "YouTube";
            case INSTAGRAM_REELS -> "Instagram Reels";
            case COMMERCIAL -> "Commercial";
            case MOTION_GRAPHICS -> "Motion Graphics";
            case CORPORATE -> "Corporate";
            case REAL_ESTATE -> "Real Estate";
        };
    }
}
