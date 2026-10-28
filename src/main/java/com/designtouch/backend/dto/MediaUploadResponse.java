package com.designtouch.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MediaUploadResponse(
        boolean success,
        String message,
        String url,
        String publicId,
        String resourceType,
        String format,
        long bytes
) {
    @JsonProperty("secureUrl")
    public String secureUrl() {
        return url;
    }
    public static MediaUploadResponse success(String url, String publicId, String resourceType, String format, long bytes) {
        return new MediaUploadResponse(
                true,
                "video".equalsIgnoreCase(resourceType) ? "Video uploaded successfully" : "Image uploaded successfully",
                url,
                publicId,
                resourceType,
                format,
                bytes
        );
    }

    public static MediaUploadResponse success(String message, String url, String publicId, String resourceType, String format, long bytes) {
        return new MediaUploadResponse(
                true,
                message,
                url,
                publicId,
                resourceType,
                format,
                bytes
        );
    }
}

