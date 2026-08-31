package com.designtouch.backend.dto;

import com.designtouch.backend.entity.AboutContent;

public record AboutContentResponse(
        String heading, String subheading, String storyParagraph1, String storyParagraph2,
        String founderName, String founderSignatureUrl, String storyVideoUrl, String storyVideoThumbnailUrl) {

    public static AboutContentResponse from(AboutContent a) {
        return new AboutContentResponse(
                a.getHeading(), a.getSubheading(), a.getStoryParagraph1(), a.getStoryParagraph2(),
                a.getFounderName(), a.getFounderSignatureUrl(), a.getStoryVideoUrl(), a.getStoryVideoThumbnailUrl());
    }
}
