package com.designtouch.backend.controller;

import com.designtouch.backend.dto.PortfolioItemRequest;
import com.designtouch.backend.dto.PortfolioItemResponse;
import com.designtouch.backend.dto.ResourceLink;
import com.designtouch.backend.entity.PortfolioCategory;
import com.designtouch.backend.entity.PortfolioItem;
import com.designtouch.backend.entity.enums.CategoryType;
import com.designtouch.backend.entity.enums.ContentType;
import com.designtouch.backend.exception.ResourceNotFoundException;
import com.designtouch.backend.repository.PortfolioCategoryRepository;
import com.designtouch.backend.repository.PortfolioItemRepository;
import com.designtouch.backend.service.MediaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioItemController {

    private final PortfolioItemRepository repository;
    private final PortfolioCategoryRepository categoryRepository;
    private final MediaService mediaService;

    // ---------- Public reads ----------

    // Home page "Featured Projects" section
    @GetMapping("/featured")
    public List<PortfolioItemResponse> getFeatured(@RequestParam(required = false) ContentType contentType) {
        List<PortfolioItem> items;
        if (contentType != null) {
            items = repository.findByContentTypeAndIsFeaturedTrueOrderByDisplayOrderAsc(contentType);
        } else {
            items = repository.findByIsFeaturedTrueOrderByDisplayOrderAsc();
        }
        return items.stream()
                .map(PortfolioItemResponse::from)
                .toList();
    }

    // Portfolio page:
    //   GET /api/portfolio?contentType=VIDEO_EDITING                               -> "ALL WORK" tab
    //   GET /api/portfolio?contentType=VIDEO_EDITING&categoryId=...                -> dynamic category filter by ID
    //   GET /api/portfolio?contentType=VIDEO_EDITING&categorySlug=youtube          -> dynamic category filter by slug
    //   GET /api/portfolio?contentType=VIDEO_EDITING&category=YOUTUBE              -> legacy category enum filter
    //   GET /api/portfolio?contentType=VIDEO_EDITING&sort=latest                   -> "Latest" sort dropdown
    @GetMapping
    public List<PortfolioItemResponse> getByFilters(
            @RequestParam ContentType contentType,
            @RequestParam(required = false) com.designtouch.backend.entity.enums.PortfolioCategory category,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String categorySlug,
            @RequestParam(required = false) String sort) {

        List<PortfolioItem> items;
        if (categoryId != null) {
            items = repository.findByContentTypeAndCategoryRef_IdOrderByDisplayOrderAsc(contentType, categoryId);
        } else if (categorySlug != null && !categorySlug.isBlank()) {
            items = repository.findByContentTypeAndCategoryRef_SlugOrderByDisplayOrderAsc(contentType, categorySlug);
        } else if (category != null) {
            items = repository.findByContentTypeAndCategoryOrderByDisplayOrderAsc(contentType, category);
        } else if ("latest".equalsIgnoreCase(sort)) {
            items = repository.findByContentTypeOrderByCreatedAtDesc(contentType);
        } else {
            items = repository.findByContentTypeOrderByDisplayOrderAsc(contentType);
        }

        return items.stream().map(PortfolioItemResponse::from).toList();
    }

    // Public single project detail route by UUID or stable human-readable slug
    @GetMapping("/{idOrSlug}")
    public PortfolioItemResponse getByIdOrSlug(@PathVariable String idOrSlug) {
        PortfolioItem item = null;
        try {
            UUID uuid = UUID.fromString(idOrSlug);
            item = repository.findById(uuid).orElse(null);
        } catch (IllegalArgumentException ignored) {
            // Not a UUID, look up by slug below
        }
        if (item == null) {
            item = repository.findBySlug(idOrSlug)
                    .orElseThrow(() -> new ResourceNotFoundException("Portfolio project not found: " + idOrSlug));
        }
        return PortfolioItemResponse.from(item);
    }

    // Direct proxy download for thumbnail image assets (bypasses browser CORS restrictions while preserving original bytes)
    @GetMapping("/{idOrSlug}/download-image")
    public ResponseEntity<byte[]> downloadThumbnailImage(@PathVariable String idOrSlug) {
        PortfolioItem item = null;
        try {
            UUID uuid = UUID.fromString(idOrSlug);
            item = repository.findById(uuid).orElse(null);
        } catch (IllegalArgumentException ignored) {
        }
        if (item == null) {
            item = repository.findBySlug(idOrSlug)
                    .orElseThrow(() -> new ResourceNotFoundException("Portfolio project not found: " + idOrSlug));
        }

        String imageUrl = item.getImageUrl() != null && !item.getImageUrl().isBlank()
                ? item.getImageUrl()
                : item.getThumbnailUrl();

        if (imageUrl == null || imageUrl.isBlank()) {
            throw new ResourceNotFoundException("No downloadable image asset available for project: " + idOrSlug);
        }

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .timeout(Duration.ofSeconds(20))
                    .GET()
                    .build();

            HttpResponse<byte[]> res = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
            if (res.statusCode() >= 400) {
                return ResponseEntity.status(res.statusCode()).build();
            }

            byte[] bytes = res.body();
            String contentTypeHeader = res.headers().firstValue("Content-Type").orElse("image/png");

            String ext = "png";
            if (contentTypeHeader.contains("jpeg") || contentTypeHeader.contains("jpg")) ext = "jpg";
            else if (contentTypeHeader.contains("webp")) ext = "webp";
            else if (contentTypeHeader.contains("png")) ext = "png";
            else if (imageUrl.contains(".jpg") || imageUrl.contains(".jpeg")) ext = "jpg";
            else if (imageUrl.contains(".webp")) ext = "webp";

            String safeName = (item.getTitle() != null && !item.getTitle().isBlank())
                    ? item.getTitle().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "")
                    : "thumbnail";
            if (safeName.isBlank()) safeName = "thumbnail";
            String filename = safeName + "." + ext;

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentTypeHeader))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                    .body(bytes);

        } catch (Exception e) {
            throw new RuntimeException("Failed to stream image download: " + e.getMessage(), e);
        }
    }

    // ---------- Admin writes (require a valid JWT — see SecurityConfig) ----------

    // Admin table view — every item regardless of contentType/featured status, latest first
    @GetMapping("/admin/all")
    public List<PortfolioItemResponse> getAllForAdmin() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(PortfolioItemResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PortfolioItemResponse create(@Valid @RequestBody PortfolioItemRequest request) {
        validateMediaRequirements(request);

        String validLiveVideoUrl = sanitizeAndValidateUrl(request.liveVideoUrl(), "Live Video URL");
        String validGoogleDriveUrl = sanitizeAndValidateGoogleDriveUrl(request.googleDriveUrl(), "Google Drive Delivery URL");
        List<ResourceLink> validResourceLinks = sanitizeResourceLinks(request.resourceLinks());

        PortfolioCategory categoryRef = resolveCategory(
                request.categoryId(),
                request.contentType(),
                request.category(),
                request.subCategory()
        );

        String slug = generateUniqueSlug(request.title(), request.slug(), null);

        com.designtouch.backend.entity.enums.VideoAspectRatio ratio = request.videoAspectRatio();
        if (request.contentType() == ContentType.VIDEO_EDITING && ratio == null) {
            ratio = com.designtouch.backend.entity.enums.VideoAspectRatio.AUTO;
        } else if (request.contentType() == ContentType.THUMBNAIL_DESIGN) {
            ratio = null;
        }

        PortfolioItem item = PortfolioItem.builder()
                .title(request.title())
                .slug(slug)
                .contentType(request.contentType())
                .categoryRef(categoryRef)
                .category(request.category() != null ? request.category() : resolveLegacyCategory(categoryRef))
                .subCategory(resolveSubCategory(request.subCategory(), categoryRef))
                .description(request.description())
                .duration(request.duration())
                .videoUrl(request.videoUrl())
                .imageUrl(request.imageUrl())
                .thumbnailUrl(request.thumbnailUrl())
                .videoPublicId(request.videoPublicId())
                .imagePublicId(request.imagePublicId())
                .liveVideoUrl(validLiveVideoUrl)
                .googleDriveUrl(validGoogleDriveUrl)
                .resourceLinks(validResourceLinks)
                .videoAspectRatio(ratio)
                .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
                .isFeatured(request.isFeatured() != null ? request.isFeatured() : false)
                .build();
        return PortfolioItemResponse.from(repository.save(item));
    }

    @PutMapping("/{id}")
    public PortfolioItemResponse update(@PathVariable UUID id, @Valid @RequestBody PortfolioItemRequest request) {
        PortfolioItem item = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio item not found: " + id));

        validateMediaRequirements(request);

        String validLiveVideoUrl = sanitizeAndValidateUrl(request.liveVideoUrl(), "Live Video URL");
        String validGoogleDriveUrl = sanitizeAndValidateGoogleDriveUrl(request.googleDriveUrl(), "Google Drive Delivery URL");
        List<ResourceLink> validResourceLinks = sanitizeResourceLinks(request.resourceLinks());

        // Cloudinary video replacement lifecycle
        if (item.getVideoPublicId() != null && !item.getVideoPublicId().isBlank()) {
            boolean videoChanged = request.videoPublicId() != null && !request.videoPublicId().equals(item.getVideoPublicId());
            boolean videoRemoved = (request.videoPublicId() == null || request.videoPublicId().isBlank()) && (request.videoUrl() == null || request.videoUrl().isBlank());
            if (videoChanged || videoRemoved) {
                mediaService.deleteVideo(item.getVideoPublicId());
            }
        }

        // Cloudinary image replacement lifecycle
        if (item.getImagePublicId() != null && !item.getImagePublicId().isBlank()) {
            boolean imageChanged = request.imagePublicId() != null && !request.imagePublicId().equals(item.getImagePublicId());
            boolean imageRemoved = (request.imagePublicId() == null || request.imagePublicId().isBlank()) && (request.imageUrl() == null || request.imageUrl().isBlank());
            if (imageChanged || imageRemoved) {
                mediaService.deleteImage(item.getImagePublicId());
            }
        }

        PortfolioCategory categoryRef = resolveCategory(
                request.categoryId(),
                request.contentType(),
                request.category(),
                request.subCategory()
        );

        String slug = (request.slug() != null && !request.slug().isBlank())
                ? generateUniqueSlug(request.title(), request.slug(), id)
                : (item.getSlug() != null ? item.getSlug() : generateUniqueSlug(request.title(), null, id));

        item.setTitle(request.title());
        item.setSlug(slug);
        item.setContentType(request.contentType());
        item.setCategoryRef(categoryRef);
        item.setCategory(request.category() != null ? request.category() : resolveLegacyCategory(categoryRef));
        item.setSubCategory(resolveSubCategory(request.subCategory(), categoryRef));
        item.setDescription(request.description());
        item.setDuration(request.duration());
        item.setVideoUrl(request.videoUrl());
        item.setImageUrl(request.imageUrl());
        item.setThumbnailUrl(request.thumbnailUrl());
        item.setVideoPublicId(request.videoPublicId());
        item.setImagePublicId(request.imagePublicId());
        item.setLiveVideoUrl(validLiveVideoUrl);
        item.setGoogleDriveUrl(validGoogleDriveUrl);
        item.setResourceLinks(validResourceLinks);
        if (request.contentType() == ContentType.VIDEO_EDITING) {
            item.setVideoAspectRatio(request.videoAspectRatio() != null ? request.videoAspectRatio() : (item.getVideoAspectRatio() != null ? item.getVideoAspectRatio() : com.designtouch.backend.entity.enums.VideoAspectRatio.AUTO));
        } else {
            item.setVideoAspectRatio(null);
        }
        if (request.displayOrder() != null) item.setDisplayOrder(request.displayOrder());
        if (request.isFeatured() != null) item.setIsFeatured(request.isFeatured());

        return PortfolioItemResponse.from(repository.save(item));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        PortfolioItem item = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio item not found: " + id));

        if (item.getVideoPublicId() != null && !item.getVideoPublicId().isBlank()) {
            mediaService.deleteVideo(item.getVideoPublicId());
        }
        if (item.getImagePublicId() != null && !item.getImagePublicId().isBlank()) {
            mediaService.deleteImage(item.getImagePublicId());
        }
        repository.delete(item);
    }

    // ---------- Helpers & Validation ----------

    private void validateMediaRequirements(PortfolioItemRequest request) {
        if (request.contentType() == ContentType.VIDEO_EDITING) {
            boolean hasVideo = (request.videoUrl() != null && !request.videoUrl().isBlank())
                    || (request.liveVideoUrl() != null && !request.liveVideoUrl().isBlank());
            if (!hasVideo) {
                throw new IllegalArgumentException("A video file upload, Cloudinary video URL, or Live Video URL is required for Video Editing projects.");
            }
        } else if (request.contentType() == ContentType.THUMBNAIL_DESIGN) {
            boolean hasImage = (request.imageUrl() != null && !request.imageUrl().isBlank())
                    || (request.thumbnailUrl() != null && !request.thumbnailUrl().isBlank());
            if (!hasImage) {
                throw new IllegalArgumentException("A thumbnail image or image URL is required for Thumbnail Design projects.");
            }
        }
    }

    private String sanitizeAndValidateUrl(String url, String fieldName) {
        if (url == null || url.isBlank()) {
            return null;
        }
        String trimmed = url.trim();
        String lower = trimmed.toLowerCase();
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
            throw new IllegalArgumentException(fieldName + " must use HTTP or HTTPS protocol.");
        }
        if (lower.startsWith("javascript:") || lower.startsWith("data:") || lower.startsWith("vbscript:")) {
            throw new IllegalArgumentException(fieldName + " contains a prohibited or dangerous URL scheme.");
        }
        try {
            URI uri = URI.create(trimmed);
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new IllegalArgumentException(fieldName + " must contain a valid domain host.");
            }
            if (uri.getUserInfo() != null) {
                throw new IllegalArgumentException(fieldName + " must not expose user credentials in the URL.");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(fieldName + " is not a valid URL: " + e.getMessage());
        }
        return trimmed;
    }

    private String sanitizeAndValidateGoogleDriveUrl(String url, String fieldName) {
        String sanitized = sanitizeAndValidateUrl(url, fieldName);
        if (sanitized == null) {
            return null;
        }
        if (!sanitized.toLowerCase().startsWith("https://")) {
            throw new IllegalArgumentException(fieldName + " must use secure HTTPS protocol.");
        }
        try {
            URI uri = URI.create(sanitized);
            String host = uri.getHost() != null ? uri.getHost().toLowerCase() : "";
            boolean isGoogleDrive = host.equals("drive.google.com")
                    || host.endsWith(".drive.google.com")
                    || host.equals("docs.google.com")
                    || host.endsWith(".docs.google.com");
            if (!isGoogleDrive) {
                throw new IllegalArgumentException(fieldName + " must be a valid Google Drive link (e.g., https://drive.google.com/... or https://docs.google.com/...).");
            }
        } catch (IllegalArgumentException iae) {
            throw iae;
        } catch (Exception e) {
            throw new IllegalArgumentException(fieldName + " is not a valid URL: " + e.getMessage());
        }
        return sanitized;
    }

    private List<ResourceLink> sanitizeResourceLinks(List<ResourceLink> links) {
        if (links == null || links.isEmpty()) {
            return List.of();
        }
        List<ResourceLink> sanitized = new ArrayList<>();
        for (int i = 0; i < links.size(); i++) {
            ResourceLink link = links.get(i);
            if (link == null) continue;
            String title = link.title() != null ? link.title().trim() : "";
            if (title.isBlank()) {
                throw new IllegalArgumentException("Resource link #" + (i + 1) + " must have a title.");
            }
            String validUrl = sanitizeAndValidateUrl(link.url(), "Resource link #" + (i + 1) + " ('" + title + "') URL");
            if (validUrl == null) {
                throw new IllegalArgumentException("Resource link #" + (i + 1) + " ('" + title + "') must have a valid URL.");
            }
            String desc = link.description() != null ? link.description().trim() : null;
            sanitized.add(new ResourceLink(title, validUrl, desc));
        }
        return sanitized;
    }

    private String generateUniqueSlug(String title, String requestedSlug, UUID existingId) {
        String base = (requestedSlug != null && !requestedSlug.isBlank())
                ? requestedSlug.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "")
                : title.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
        if (base.isBlank()) {
            base = "project";
        }
        String candidate = base;
        int counter = 1;
        while (true) {
            Optional<PortfolioItem> existing = repository.findBySlug(candidate);
            if (existing.isEmpty() || (existingId != null && existing.get().getId().equals(existingId))) {
                return candidate;
            }
            candidate = base + "-" + counter++;
        }
    }

    private PortfolioCategory resolveCategory(
            UUID categoryId,
            ContentType contentType,
            com.designtouch.backend.entity.enums.PortfolioCategory legacyCategory,
            String subCategory) {

        if (categoryId != null) {
            PortfolioCategory cat = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));

            // Validate separation: Video categories only for VIDEO_EDITING, Thumbnail categories only for THUMBNAIL_DESIGN
            if (cat.getCategoryType() == CategoryType.VIDEO_EDITING && contentType != ContentType.VIDEO_EDITING) {
                throw new IllegalArgumentException("Cannot assign Video Editing category '" + cat.getName() + "' to Thumbnail Design project.");
            }
            if (cat.getCategoryType() == CategoryType.THUMBNAIL_DESIGN && contentType != ContentType.THUMBNAIL_DESIGN) {
                throw new IllegalArgumentException("Cannot assign Thumbnail Design category '" + cat.getName() + "' to Video Editing project.");
            }
            return cat;
        }

        // Fallback for legacy requests: if legacyCategory enum is provided for video editing
        if (contentType == ContentType.VIDEO_EDITING && legacyCategory != null) {
            String slug = legacyCategory.name().toLowerCase().replace('_', '-');
            return categoryRepository.findBySlugAndCategoryType(slug, CategoryType.VIDEO_EDITING).orElse(null);
        }

        // Fallback for legacy thumbnail requests: if subCategory is provided
        if (contentType == ContentType.THUMBNAIL_DESIGN && subCategory != null && !subCategory.isBlank()) {
            String slug = subCategory.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
            return categoryRepository.findBySlugAndCategoryType(slug, CategoryType.THUMBNAIL_DESIGN).orElse(null);
        }

        return null;
    }

    private com.designtouch.backend.entity.enums.PortfolioCategory resolveLegacyCategory(PortfolioCategory categoryRef) {
        if (categoryRef == null || categoryRef.getCategoryType() != CategoryType.VIDEO_EDITING) {
            return null;
        }
        try {
            return com.designtouch.backend.entity.enums.PortfolioCategory.valueOf(categoryRef.getSlug().toUpperCase().replace('-', '_'));
        } catch (Exception e) {
            return null;
        }
    }

    private String resolveSubCategory(String explicitSubCategory, PortfolioCategory categoryRef) {
        if (categoryRef != null && categoryRef.getCategoryType() == CategoryType.THUMBNAIL_DESIGN) {
            return categoryRef.getName();
        }
        return explicitSubCategory;
    }
}
