package com.designtouch.backend.dto;

public record AboutContentRequest(
        String heading,
        String subheading,
        String ctaText,
        String ctaLink,
        String heroBannerImageUrl,
        String heroBannerImagePublicId,
        String storyHeading,
        String storyParagraph1,
        String storyParagraph2,
        String storyImageUrl,
        String founderName,
        String founderDesignation,
        String founderSignatureUrl,
        String storyVideoUrl,
        String storyVideoThumbnailUrl) {
}

