package com.designtouch.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.designtouch.backend.dto.MediaMigrationReport;
import com.designtouch.backend.dto.ProjectMediaInventoryItem;
import com.designtouch.backend.entity.PortfolioItem;
import com.designtouch.backend.entity.enums.ContentType;
import com.designtouch.backend.repository.PortfolioItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaMigrationService {

    private final PortfolioItemRepository itemRepository;
    private final Cloudinary cloudinary;

    @Transactional(readOnly = true)
    public MediaMigrationReport generateInventory() {
        List<PortfolioItem> items = itemRepository.findAll();
        List<ProjectMediaInventoryItem> inventory = new ArrayList<>();

        for (PortfolioItem item : items) {
            String categoryName = item.getCategoryRef() != null
                    ? item.getCategoryRef().getName()
                    : (item.getSubCategory() != null ? item.getSubCategory() : (item.getCategory() != null ? item.getCategory().name() : "Uncategorized"));

            if (item.getContentType() == ContentType.VIDEO_EDITING) {
                // Video asset check
                inventory.add(classifyMedia(
                        item,
                        "VIDEO",
                        item.getVideoUrl(),
                        item.getVideoPublicId(),
                        categoryName
                ));
                // Optional Poster asset check
                if (item.getImageUrl() != null && !item.getImageUrl().isBlank()) {
                    inventory.add(classifyMedia(
                            item,
                            "POSTER_IMAGE",
                            item.getImageUrl(),
                            item.getImagePublicId(),
                            categoryName
                    ));
                }
            } else {
                // Thumbnail Design image check
                String imageUrl = item.getImageUrl() != null && !item.getImageUrl().isBlank()
                        ? item.getImageUrl()
                        : item.getThumbnailUrl();
                inventory.add(classifyMedia(
                        item,
                        "THUMBNAIL_IMAGE",
                        imageUrl,
                        item.getImagePublicId(),
                        categoryName
                ));
            }
        }

        int migrated = 0;
        int skipped = 0;
        int error = 0;

        for (ProjectMediaInventoryItem inv : inventory) {
            if ("ALREADY_MIGRATED".equals(inv.status()) || "MIGRATED".equals(inv.status())) {
                migrated++;
            } else if ("ERROR".equals(inv.status())) {
                error++;
            } else {
                skipped++;
            }
        }

        return new MediaMigrationReport(items.size(), migrated, skipped, error, true, inventory);
    }

    @Transactional
    public MediaMigrationReport migrateMedia(boolean dryRun) {
        log.info("Starting portfolio media migration (dryRun={})", dryRun);
        List<PortfolioItem> items = itemRepository.findAll();
        List<ProjectMediaInventoryItem> reportItems = new ArrayList<>();

        int migratedCount = 0;
        int skippedCount = 0;
        int errorCount = 0;

        for (PortfolioItem item : items) {
            String categoryName = item.getCategoryRef() != null
                    ? item.getCategoryRef().getName()
                    : (item.getSubCategory() != null ? item.getSubCategory() : (item.getCategory() != null ? item.getCategory().name() : "Uncategorized"));

            if (item.getContentType() == ContentType.VIDEO_EDITING) {
                // 1. Process Video
                ProjectMediaInventoryItem vidItem = processMediaItem(
                        item,
                        "VIDEO",
                        item.getVideoUrl(),
                        item.getVideoPublicId(),
                        categoryName,
                        "designtouch/portfolio",
                        "video",
                        dryRun
                );
                reportItems.add(vidItem);
                if ("MIGRATED".equals(vidItem.status()) || "ALREADY_MIGRATED".equals(vidItem.status())) migratedCount++;
                else if ("ERROR".equals(vidItem.status())) errorCount++;
                else skippedCount++;

                // 2. Process Poster if present
                if (item.getImageUrl() != null && !item.getImageUrl().isBlank()) {
                    ProjectMediaInventoryItem posterItem = processMediaItem(
                            item,
                            "POSTER_IMAGE",
                            item.getImageUrl(),
                            item.getImagePublicId(),
                            categoryName,
                            "designtouch/portfolio",
                            "image",
                            dryRun
                    );
                    reportItems.add(posterItem);
                    if ("MIGRATED".equals(posterItem.status()) || "ALREADY_MIGRATED".equals(posterItem.status())) migratedCount++;
                    else if ("ERROR".equals(posterItem.status())) errorCount++;
                    else skippedCount++;
                }
            } else {
                // Thumbnail Design
                String imageUrl = item.getImageUrl() != null && !item.getImageUrl().isBlank()
                        ? item.getImageUrl()
                        : item.getThumbnailUrl();
                ProjectMediaInventoryItem thumbItem = processMediaItem(
                        item,
                        "THUMBNAIL_IMAGE",
                        imageUrl,
                        item.getImagePublicId(),
                        categoryName,
                        "designtouch/thumbnails",
                        "image",
                        dryRun
                );
                reportItems.add(thumbItem);
                if ("MIGRATED".equals(thumbItem.status()) || "ALREADY_MIGRATED".equals(thumbItem.status())) migratedCount++;
                else if ("ERROR".equals(thumbItem.status())) errorCount++;
                else skippedCount++;
            }
        }

        return new MediaMigrationReport(items.size(), migratedCount, skippedCount, errorCount, dryRun, reportItems);
    }

    private ProjectMediaInventoryItem classifyMedia(
            PortfolioItem item,
            String mediaField,
            String url,
            String publicId,
            String categoryName) {

        if (url == null || url.isBlank()) {
            return new ProjectMediaInventoryItem(
                    item.getId(),
                    item.getTitle(),
                    item.getContentType(),
                    categoryName,
                    mediaField,
                    null,
                    null,
                    "NO_MEDIA",
                    "SKIPPED_NO_MEDIA",
                    null,
                    null,
                    "No media currently configured. Upload required manually through Admin Panel."
            );
        }

        if (url.contains("cloudinary.com") || (publicId != null && !publicId.isBlank())) {
            return new ProjectMediaInventoryItem(
                    item.getId(),
                    item.getTitle(),
                    item.getContentType(),
                    categoryName,
                    mediaField,
                    url,
                    publicId,
                    "CLOUDINARY",
                    "ALREADY_MIGRATED",
                    url,
                    publicId,
                    "Already securely hosted on Cloudinary with public ID: " + publicId
            );
        }

        File localFile = resolveLocalFile(url);
        if (localFile != null) {
            return new ProjectMediaInventoryItem(
                    item.getId(),
                    item.getTitle(),
                    item.getContentType(),
                    categoryName,
                    mediaField,
                    url,
                    publicId,
                    "LOCAL_FILE",
                    "READY_FOR_MIGRATION",
                    null,
                    null,
                    "Local source file verified on disk: " + localFile.getName() + " (" + localFile.length() + " bytes)"
            );
        }

        if (url.startsWith("/") || !url.startsWith("http")) {
            return new ProjectMediaInventoryItem(
                    item.getId(),
                    item.getTitle(),
                    item.getContentType(),
                    categoryName,
                    mediaField,
                    url,
                    publicId,
                    "LOCAL_FILE",
                    "SKIPPED_SOURCE_NOT_FOUND",
                    null,
                    null,
                    "Local path specified but physical file does not exist on disk: " + url + ". Record preserved."
            );
        }

        return new ProjectMediaInventoryItem(
                item.getId(),
                item.getTitle(),
                item.getContentType(),
                categoryName,
                mediaField,
                url,
                publicId,
                "EXTERNAL_PLACEHOLDER",
                "SKIPPED_EXTERNAL_PLACEHOLDER",
                null,
                null,
                "Remote placeholder URL preserved; no local physical file on disk. Admin can upload replacement manually."
        );
    }

    private ProjectMediaInventoryItem processMediaItem(
            PortfolioItem item,
            String mediaField,
            String url,
            String publicId,
            String categoryName,
            String targetFolder,
            String resourceType,
            boolean dryRun) {

        ProjectMediaInventoryItem base = classifyMedia(item, mediaField, url, publicId, categoryName);

        // If not ready for migration, return classification as-is
        if (!"READY_FOR_MIGRATION".equals(base.status())) {
            return base;
        }

        File localFile = resolveLocalFile(url);
        if (localFile == null) {
            return new ProjectMediaInventoryItem(
                    item.getId(),
                    item.getTitle(),
                    item.getContentType(),
                    categoryName,
                    mediaField,
                    url,
                    publicId,
                    "LOCAL_FILE",
                    "SKIPPED_SOURCE_NOT_FOUND",
                    null,
                    null,
                    "Local file verification failed during execution."
            );
        }

        if (dryRun) {
            return new ProjectMediaInventoryItem(
                    item.getId(),
                    item.getTitle(),
                    item.getContentType(),
                    categoryName,
                    mediaField,
                    url,
                    publicId,
                    "LOCAL_FILE",
                    "READY_FOR_MIGRATION",
                    "[WILL_UPLOAD_TO_CLOUDINARY: " + targetFolder + "]",
                    "[GENERATED_UPON_UPLOAD]",
                    "Dry run: Local file " + localFile.getName() + " (" + localFile.length() + " bytes) is verified and ready for upload."
            );
        }

        // Live execution: upload file bytes to Cloudinary
        try {
            log.info("Uploading local file '{}' to Cloudinary folder '{}' for project '{}'",
                    localFile.getAbsolutePath(), targetFolder, item.getTitle());

            byte[] bytes = Files.readAllBytes(localFile.toPath());
            Map<String, Object> params = ObjectUtils.asMap(
                    "resource_type", resourceType,
                    "folder", targetFolder,
                    "overwrite", false
            );

            Map<?, ?> uploadResult = cloudinary.uploader().upload(bytes, params);
            String secureUrl = (String) uploadResult.get("secure_url");
            if (secureUrl == null || secureUrl.isBlank()) {
                secureUrl = (String) uploadResult.get("url");
            }
            String newPublicId = (String) uploadResult.get("public_id");

            if (secureUrl == null || secureUrl.isBlank() || newPublicId == null || newPublicId.isBlank()) {
                throw new RuntimeException("Cloudinary upload did not return valid URL and publicId");
            }

            // Update item in database
            if ("VIDEO".equals(mediaField)) {
                item.setVideoUrl(secureUrl);
                item.setVideoPublicId(newPublicId);
            } else if ("POSTER_IMAGE".equals(mediaField)) {
                item.setImageUrl(secureUrl);
                item.setImagePublicId(newPublicId);
            } else {
                item.setImageUrl(secureUrl);
                item.setThumbnailUrl(secureUrl);
                item.setImagePublicId(newPublicId);
            }
            itemRepository.save(item);
            log.info("Project '{}' updated with new Cloudinary URL: {}", item.getTitle(), secureUrl);

            return new ProjectMediaInventoryItem(
                    item.getId(),
                    item.getTitle(),
                    item.getContentType(),
                    categoryName,
                    mediaField,
                    url,
                    publicId,
                    "LOCAL_FILE",
                    "MIGRATED",
                    secureUrl,
                    newPublicId,
                    "Successfully uploaded to Cloudinary (" + newPublicId + ") and persisted in database."
            );
        } catch (IOException e) {
            log.error("Failed to upload local file '{}': {}", localFile.getAbsolutePath(), e.getMessage());
            return new ProjectMediaInventoryItem(
                    item.getId(),
                    item.getTitle(),
                    item.getContentType(),
                    categoryName,
                    mediaField,
                    url,
                    publicId,
                    "LOCAL_FILE",
                    "ERROR",
                    null,
                    null,
                    "Upload error: " + e.getMessage()
            );
        }
    }

    public File resolveLocalFile(String path) {
        if (path == null || path.isBlank()) return null;
        if (path.startsWith("http://") || path.startsWith("https://")) return null;

        String clean = path.startsWith("/") ? path.substring(1) : path;

        // Try various relative positions
        List<Path> candidates = List.of(
                Path.of(clean),
                Path.of("designtouch-frontend", "public", clean),
                Path.of("..", "designtouch-frontend", "public", clean),
                Path.of("public", clean),
                Path.of("..", "public", clean),
                Path.of("src", "main", "resources", "static", clean)
        );

        for (Path candidate : candidates) {
            try {
                if (Files.exists(candidate) && Files.isRegularFile(candidate)) {
                    return candidate.toFile();
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }
}

