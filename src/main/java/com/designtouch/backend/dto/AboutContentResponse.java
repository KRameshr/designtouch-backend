package com.designtouch.backend.dto;

import com.designtouch.backend.entity.AboutContent;

public record AboutContentResponse(
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

    public static AboutContentResponse from(AboutContent a) {
        String storyImg = a.getStoryImageUrl() != null ? a.getStoryImageUrl() : a.getStoryVideoThumbnailUrl();
        return new AboutContentResponse(
                a.getHeading(),
                a.getSubheading(),
                a.getCtaText() != null ? a.getCtaText() : "Let's Create Together",
                a.getCtaLink() != null ? a.getCtaLink() : "/contact",
                a.getHeroBannerImageUrl(),
                a.getHeroBannerImagePublicId(),
                a.getStoryHeading() != null ? a.getStoryHeading() : "Turning Ideas Into Impact.",
                a.getStoryParagraph1(),
                a.getStoryParagraph2(),
                storyImg,
                a.getFounderName(),
                a.getFounderDesignation() != null ? a.getFounderDesignation() : "Founder, DesignTouch+",
                a.getFounderSignatureUrl(),
                a.getStoryVideoUrl(),
                a.getStoryVideoThumbnailUrl() != null ? a.getStoryVideoThumbnailUrl() : storyImg);
    }
}
