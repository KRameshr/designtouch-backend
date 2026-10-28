package com.designtouch.backend.controller;

import com.designtouch.backend.dto.MediaUploadResponse;
import com.designtouch.backend.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    // ---------- Video Uploads ----------

    @PostMapping(value = "/upload/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaUploadResponse> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false) String folder) {
        MediaUploadResponse response = (folder != null && !folder.isBlank())
                ? mediaService.uploadVideo(file, folder)
                : mediaService.uploadVideo(file);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/upload/portfolio-video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaUploadResponse> uploadPortfolioVideo(@RequestParam("file") MultipartFile file) {
        MediaUploadResponse response = mediaService.uploadPortfolioVideo(file);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/upload/portfolio-intro-video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaUploadResponse> uploadPortfolioIntroVideo(@RequestParam("file") MultipartFile file) {
        MediaUploadResponse response = mediaService.uploadPortfolioIntroVideo(file);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/video")
    public ResponseEntity<Void> deleteVideo(@RequestParam("publicId") String publicId) {
        mediaService.deleteVideo(publicId);
        return ResponseEntity.noContent().build();
    }

    // ---------- Image Uploads ----------

    @PostMapping(value = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaUploadResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false) String folder) {
        MediaUploadResponse response = (folder != null && !folder.isBlank())
                ? mediaService.uploadImage(file, folder)
                : mediaService.uploadImage(file);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/upload/addon-icon", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaUploadResponse> uploadAddOnIcon(@RequestParam("file") MultipartFile file) {
        MediaUploadResponse response = mediaService.uploadAddOnIcon(file);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/upload/thumbnail-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaUploadResponse> uploadThumbnailImage(@RequestParam("file") MultipartFile file) {
        MediaUploadResponse response = mediaService.uploadThumbnailImage(file);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/image")
    public ResponseEntity<Void> deleteImage(@RequestParam("publicId") String publicId) {
        mediaService.deleteImage(publicId);
        return ResponseEntity.noContent().build();
    }
}
