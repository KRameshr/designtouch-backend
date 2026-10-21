package com.designtouch.backend.dto;

public record MediaUploadResponse(
        boolean success,
        String message,
        String url,
        String publicId,
        String resourceType,
        String format,
        long bytes
) {
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

