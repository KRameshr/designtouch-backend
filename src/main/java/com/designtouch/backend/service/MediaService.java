package com.designtouch.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.designtouch.backend.dto.MediaUploadResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MediaService {

    private static final Logger log = LoggerFactory.getLogger(MediaService.class);

    private static final List<String> ALLOWED_VIDEO_EXTENSIONS = List.of("mp4", "webm", "mov");
    private static final List<String> ALLOWED_VIDEO_MIME_TYPES = List.of(
            "video/mp4",
            "video/webm",
            "video/quicktime",
            "video/x-quicktime",
            "video/mov"
    );

    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = List.of("jpg", "jpeg", "png", "webp");
    private static final List<String> ALLOWED_IMAGE_MIME_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final Cloudinary cloudinary;

    @Value("${app.media.max-video-size-bytes:104857600}") // 100 MB default
    private long maxVideoSizeBytes;

    @Value("${app.media.max-image-size-bytes:10485760}") // 10 MB default
    private long maxImageSizeBytes;

    @Value("${app.media.video-folder:designtouch/videos}")
    private String videoFolder;

    @Value("${app.media.thumbnail-folder:designtouch/thumbnails}")
    private String thumbnailFolder;

    // ---------- Video operations ----------

    public MediaUploadResponse uploadVideo(MultipartFile file) {
        return uploadVideo(file, videoFolder);
    }

    public MediaUploadResponse uploadPortfolioVideo(MultipartFile file) {
        return uploadVideo(file, "designtouch/portfolio");
    }

    public MediaUploadResponse uploadPortfolioIntroVideo(MultipartFile file) {
        return uploadVideo(file, "designtouch/portfolio-intro");
    }

    public MediaUploadResponse uploadVideo(MultipartFile file, String targetFolder) {
        validateVideoFile(file);
        String folder = (targetFolder != null && !targetFolder.isBlank()) ? targetFolder : videoFolder;

        try {
            log.info("Initiating video upload to Cloudinary (filename: {}, size: {} bytes, folder: {})",
                    file.getOriginalFilename(), file.getSize(), folder);

            Map<String, Object> params = ObjectUtils.asMap(
                    "resource_type", "video",
                    "folder", folder,
                    "overwrite", false
            );

            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

            String secureUrl = (String) uploadResult.get("secure_url");
            if (secureUrl == null || secureUrl.isBlank()) {
                secureUrl = (String) uploadResult.get("url");
            }
            String publicId = (String) uploadResult.get("public_id");
            String resourceType = (String) uploadResult.get("resource_type");
            String format = (String) uploadResult.get("format");
            Number bytesVal = (Number) uploadResult.get("bytes");
            long bytes = bytesVal != null ? bytesVal.longValue() : file.getSize();

            log.info("Cloudinary video upload successful (publicId: {}, resourceType: {})", publicId, resourceType);

            return MediaUploadResponse.success(
                    secureUrl,
                    publicId,
                    resourceType != null ? resourceType : "video",
                    format,
                    bytes
            );
        } catch (IOException e) {
            log.error("Failed to upload video to Cloudinary: {}", e.getMessage());
            throw new RuntimeException("Failed to upload video to Cloudinary. Please try again later.");
        }
    }

    public void deleteVideo(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        try {
            log.info("Deleting video from Cloudinary (publicId: {})", publicId);
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "video"));
            log.info("Cloudinary video deletion result: {}", result);
        } catch (IOException e) {
            log.error("Failed to delete video from Cloudinary (publicId: {}): {}", publicId, e.getMessage());
            throw new RuntimeException("Failed to delete video from Cloudinary: " + e.getMessage(), e);
        }
    }

    // ---------- Image operations ----------

    public MediaUploadResponse uploadThumbnailImage(MultipartFile file) {
        return uploadImage(file, thumbnailFolder != null && !thumbnailFolder.isBlank() ? thumbnailFolder : "designtouch/thumbnails");
    }

    public MediaUploadResponse uploadImage(MultipartFile file) {
        return uploadImage(file, thumbnailFolder);
    }

    public MediaUploadResponse uploadImage(MultipartFile file, String targetFolder) {
        validateImageFile(file);
        String folder = (targetFolder != null && !targetFolder.isBlank()) ? targetFolder : "designtouch/thumbnails";

        try {
            log.info("Initiating image upload to Cloudinary (filename: {}, size: {} bytes, folder: {})",
                    file.getOriginalFilename(), file.getSize(), folder);

            Map<String, Object> params = ObjectUtils.asMap(
                    "resource_type", "image",
                    "folder", folder,
                    "overwrite", false
            );

            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

            String secureUrl = (String) uploadResult.get("secure_url");
            if (secureUrl == null || secureUrl.isBlank()) {
                secureUrl = (String) uploadResult.get("url");
            }
            String publicId = (String) uploadResult.get("public_id");
            String resourceType = (String) uploadResult.get("resource_type");
            String format = (String) uploadResult.get("format");
            Number bytesVal = (Number) uploadResult.get("bytes");
            long bytes = bytesVal != null ? bytesVal.longValue() : file.getSize();

            log.info("Cloudinary image upload successful (publicId: {}, resourceType: {})", publicId, resourceType);

            return MediaUploadResponse.success(
                    secureUrl,
                    publicId,
                    resourceType != null ? resourceType : "image",
                    format,
                    bytes
            );
        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary: {}", e.getMessage());
            throw new RuntimeException("Failed to upload image to Cloudinary. Please try again later.");
        }
    }

    public void deleteImage(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        try {
            log.info("Deleting image from Cloudinary (publicId: {})", publicId);
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
            log.info("Cloudinary image deletion result: {}", result);
        } catch (IOException e) {
            log.error("Failed to delete image from Cloudinary (publicId: {}): {}", publicId, e.getMessage());
            throw new RuntimeException("Failed to delete image from Cloudinary: " + e.getMessage(), e);
        }
    }

    // ---------- Validations ----------

    private void validateVideoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Video file is required and cannot be empty.");
        }

        if (file.getSize() > maxVideoSizeBytes) {
            long maxMb = maxVideoSizeBytes / (1024 * 1024);
            throw new IllegalArgumentException("File size exceeds the maximum allowed limit of " + maxMb + " MB.");
        }

        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);

        boolean mimeValid = contentType != null && (
                contentType.toLowerCase(Locale.ROOT).startsWith("video/") ||
                ALLOWED_VIDEO_MIME_TYPES.contains(contentType.toLowerCase(Locale.ROOT))
        );

        boolean extValid = extension != null && ALLOWED_VIDEO_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT));

        if (!mimeValid && !extValid) {
            throw new IllegalArgumentException("Invalid video format. Allowed formats: MP4, WebM, MOV.");
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required and cannot be empty.");
        }

        if (file.getSize() > maxImageSizeBytes) {
            long maxMb = maxImageSizeBytes / (1024 * 1024);
            throw new IllegalArgumentException("File size exceeds the maximum allowed limit of " + maxMb + " MB.");
        }

        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);

        boolean mimeValid = contentType != null && (
                ALLOWED_IMAGE_MIME_TYPES.contains(contentType.toLowerCase(Locale.ROOT))
        );

        boolean extValid = extension != null && ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT));

        if (!mimeValid && !extValid) {
            throw new IllegalArgumentException("Invalid image format. Allowed formats: JPEG, PNG, WebP.");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return null;
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
