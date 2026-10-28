package com.designtouch.backend.service;

import com.designtouch.backend.dto.BlogPostDetailResponse;
import com.designtouch.backend.dto.BlogPostRequest;
import com.designtouch.backend.dto.BlogPostSummaryResponse;
import com.designtouch.backend.dto.CategoryCountResponse;
import com.designtouch.backend.entity.BlogCategory;
import com.designtouch.backend.entity.BlogPost;
import com.designtouch.backend.entity.enums.BlogPostStatus;
import com.designtouch.backend.exception.ResourceNotFoundException;
import com.designtouch.backend.repository.BlogCategoryRepository;
import com.designtouch.backend.repository.BlogPostRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogPostService {

    private static final Logger log = LoggerFactory.getLogger(BlogPostService.class);

    private final BlogPostRepository repository;
    private final BlogCategoryRepository blogCategoryRepository;
    private final MediaService mediaService;

    // ---------- Public reads ----------

    @Transactional(readOnly = true)
    public Page<BlogPostSummaryResponse> getPage(String category, Pageable pageable) {
        Page<BlogPost> page = (category != null && !category.isBlank())
                ? repository.findPublicPostsByCategory(category.trim(), pageable)
                : repository.findPublicPosts(pageable);
        return page.map(BlogPostSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<BlogPostSummaryResponse> search(String keyword, String category, Pageable pageable) {
        Page<BlogPost> page = (category != null && !category.isBlank())
                ? repository.searchPublicWithCategory(keyword.trim(), category.trim(), pageable)
                : repository.searchPublic(keyword.trim(), pageable);
        return page.map(BlogPostSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<BlogPostSummaryResponse> search(String keyword, Pageable pageable) {
        return search(keyword, null, pageable);
    }

    @Transactional(readOnly = true)
    public List<BlogPostSummaryResponse> getPopular() {
        return repository.findTopPublicPosts(PageRequest.of(0, 5))
                .stream()
                .map(BlogPostSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryCountResponse> getCategoryCounts() {
        List<BlogCategory> categories = blogCategoryRepository.findAllByOrderByDisplayOrderAsc();
        if (categories.isEmpty()) {
            return repository.countPublicByCategory()
                    .stream()
                    .map(c -> new CategoryCountResponse(c.getCategory(), c.getName(), c.getCount()))
                    .toList();
        }

        Map<UUID, Long> countMap = repository.countPublicByCategoryId().stream()
                .collect(Collectors.toMap(
                        BlogPostRepository.CategoryCountById::getCategoryId,
                        BlogPostRepository.CategoryCountById::getCount,
                        (a, b) -> a));

        return categories.stream()
                .map(cat -> new CategoryCountResponse(
                        cat.getSlug(),
                        cat.getName(),
                        countMap.getOrDefault(cat.getId(), 0L)))
                .toList();
    }

    // Fetches the post if publicly visible, converts to DTO, and bumps the view counter
    @Transactional
    public BlogPostDetailResponse getBySlugAndIncrementViews(String slug) {
        BlogPost post = repository.findPublicBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post not found: " + slug));

        post.setViewCount(post.getViewCount() + 1);
        return BlogPostDetailResponse.from(post);
    }

    // ---------- Background Scheduler ----------

    /**
     * Checks once every minute for posts scheduled for publication that have reached their scheduled time.
     * Transitions their status to PUBLISHED and sets isPublished to true.
     */
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void publishScheduledPosts() {
        List<BlogPost> pending = repository.findPendingScheduledPosts();
        if (pending.isEmpty()) {
            return;
        }

        log.info("Found {} scheduled blog post(s) ready to publish", pending.size());
        OffsetDateTime now = OffsetDateTime.now();
        for (BlogPost post : pending) {
            post.setStatus(BlogPostStatus.PUBLISHED);
            post.setIsPublished(true);
            if (post.getPublishedAt() == null || post.getPublishedAt().isAfter(now)) {
                post.setPublishedAt(post.getScheduledFor() != null ? post.getScheduledFor() : now);
            }
            repository.save(post);
            log.info("Auto-published scheduled blog post '{}' (id: {}, slug: {})",
                    post.getTitle(), post.getId(), post.getSlug());
        }
    }

    // ---------- Admin operations ----------

    @Transactional(readOnly = true)
    public List<BlogPostSummaryResponse> getAllForAdmin() {
        return repository.findAllByOrderByPublishedAtDesc()
                .stream()
                .map(BlogPostSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public BlogPostDetailResponse getByIdForAdmin(UUID id) {
        BlogPost post = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post not found: " + id));
        return BlogPostDetailResponse.from(post);
    }

    @Transactional
    public BlogPostDetailResponse create(BlogPostRequest request) {
        String slug = request.slug() != null ? request.slug().trim().toLowerCase() : "";
        if (slug.isBlank()) {
            throw new IllegalArgumentException("Article URL slug cannot be empty.");
        }
        if (repository.existsBySlugIgnoreCase(slug)) {
            throw new IllegalArgumentException("An article with slug '" + slug + "' already exists. Please choose a unique slug.");
        }

        BlogPostStatus status = request.status();
        if (status == null) {
            status = Boolean.FALSE.equals(request.isPublished()) ? BlogPostStatus.DRAFT : BlogPostStatus.PUBLISHED;
        }

        boolean isPublished = (status == BlogPostStatus.PUBLISHED);
        OffsetDateTime publishedAt = request.publishedAt();
        if (publishedAt == null) {
            publishedAt = OffsetDateTime.now();
        }

        List<String> tags = sanitizeKeywords(request.tags());
        List<String> focusKeywords = sanitizeKeywords(request.focusKeywords());

        BlogCategory categoryRef = null;
        String categorySlug = request.category() != null ? request.category().trim() : null;
        if (request.categoryId() != null) {
            categoryRef = blogCategoryRepository.findById(request.categoryId()).orElse(null);
            if (categoryRef != null) {
                categorySlug = categoryRef.getSlug();
            }
        }
        
        if (categoryRef == null && categorySlug != null && !categorySlug.isBlank()) {
            final String slugLookup = categorySlug;
            categoryRef = blogCategoryRepository.findBySlugIgnoreCase(slugLookup)
                    .or(() -> blogCategoryRepository.findByNameIgnoreCase(slugLookup))
                    .or(() -> blogCategoryRepository.findBySlugIgnoreCase(slugLookup.toUpperCase().replace('-', '_')))
                    .orElse(null);
            if (categoryRef != null) {
                categorySlug = categoryRef.getSlug();
            }
        }

        if (categorySlug == null || categorySlug.isBlank()) {
            categorySlug = "VIDEO_EDITING";
            if (categoryRef == null) {
                categoryRef = blogCategoryRepository.findBySlugIgnoreCase("VIDEO_EDITING").orElse(null);
            }
        }

        BlogPost post = BlogPost.builder()
                .title(request.title().trim())
                .slug(slug)
                .excerpt(request.excerpt() != null && !request.excerpt().isBlank() ? request.excerpt().trim() : null)
                .content(request.content())
                .coverImageUrl(request.coverImageUrl() != null && !request.coverImageUrl().isBlank() ? request.coverImageUrl().trim() : null)
                .coverImagePublicId(request.coverImagePublicId() != null && !request.coverImagePublicId().isBlank() ? request.coverImagePublicId().trim() : null)
                .category(categorySlug)
                .categoryRef(categoryRef)
                .readingTime(request.readingTime())
                .status(status)
                .scheduledFor(status == BlogPostStatus.SCHEDULED ? request.scheduledFor() : null)
                .isPublished(isPublished)
                .publishedAt(publishedAt)
                .viewCount(0)
                .seoTitle(request.seoTitle() != null && !request.seoTitle().isBlank() ? request.seoTitle().trim() : null)
                .metaDescription(request.metaDescription() != null && !request.metaDescription().isBlank() ? request.metaDescription().trim() : null)
                .tags(tags)
                .focusKeywords(focusKeywords)
                .build();

        return BlogPostDetailResponse.from(repository.save(post));
    }

    @Transactional
    public BlogPostDetailResponse update(UUID id, BlogPostRequest request) {
        BlogPost post = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post not found: " + id));

        // Preserve untouched fields
        if (request.title() != null && !request.title().isBlank()) {
            post.setTitle(request.title().trim());
        }
        if (request.slug() != null && !request.slug().isBlank()) {
            String slug = request.slug().trim().toLowerCase();
            if (repository.existsBySlugIgnoreCaseAndIdNot(slug, id)) {
                throw new IllegalArgumentException("An article with slug '" + slug + "' already exists. Please choose a unique slug.");
            }
            post.setSlug(slug);
        }
        if (request.excerpt() != null) {
            post.setExcerpt(request.excerpt().trim());
        }
        if (request.content() != null && !request.content().isBlank()) {
            post.setContent(request.content());
        }
        if (request.categoryId() != null) {
            BlogCategory cat = blogCategoryRepository.findById(request.categoryId()).orElse(null);
            if (cat != null) {
                post.setCategoryRef(cat);
                post.setCategory(cat.getSlug());
            }
        } else if (request.category() != null && !request.category().isBlank()) {
            String catInput = request.category().trim();
            BlogCategory cat = blogCategoryRepository.findBySlugIgnoreCase(catInput)
                    .or(() -> blogCategoryRepository.findByNameIgnoreCase(catInput))
                    .or(() -> blogCategoryRepository.findBySlugIgnoreCase(catInput.toUpperCase().replace('-', '_')))
                    .orElse(null);
            if (cat != null) {
                post.setCategoryRef(cat);
                post.setCategory(cat.getSlug());
            } else {
                post.setCategory(catInput);
            }
        }
        if (request.readingTime() != null) {
            post.setReadingTime(request.readingTime());
        }

        // Cover image & Cloudinary public ID handling
        String newUrl = request.coverImageUrl() != null ? request.coverImageUrl().trim() : null;
        String newPublicId = request.coverImagePublicId() != null ? request.coverImagePublicId().trim() : null;

        if (request.coverImageUrl() != null || request.coverImagePublicId() != null) {
            String oldPublicId = post.getCoverImagePublicId();
            // If image is replaced or removed, attempt to delete old asset from Cloudinary if not shared
            if (oldPublicId != null && !oldPublicId.isBlank() && !oldPublicId.equals(newPublicId)) {
                long otherUsage = repository.countByCoverImagePublicIdAndIdNot(oldPublicId, post.getId());
                if (otherUsage == 0) {
                    try {
                        log.info("Deleting replaced blog cover image from Cloudinary (publicId: {})", oldPublicId);
                        mediaService.deleteImage(oldPublicId);
                    } catch (Exception e) {
                        log.warn("Failed to delete old Cloudinary image (publicId: {}): {}", oldPublicId, e.getMessage());
                    }
                } else {
                    log.info("Preserving old Cloudinary cover image '{}' as it is shared by {} other post(s)", oldPublicId, otherUsage);
                }
            }
            post.setCoverImageUrl(newUrl != null && !newUrl.isBlank() ? newUrl : null);
            post.setCoverImagePublicId(newPublicId != null && !newPublicId.isBlank() ? newPublicId : null);
        }

        // Status & Scheduling
        if (request.status() != null) {
            post.setStatus(request.status());
            if (request.status() == BlogPostStatus.PUBLISHED) {
                post.setIsPublished(true);
                if (request.publishedAt() != null) {
                    post.setPublishedAt(request.publishedAt());
                } else if (post.getPublishedAt() == null) {
                    post.setPublishedAt(OffsetDateTime.now());
                }
                post.setScheduledFor(null);
            } else if (request.status() == BlogPostStatus.SCHEDULED) {
                post.setIsPublished(false);
                post.setScheduledFor(request.scheduledFor());
            } else {
                post.setIsPublished(false);
            }
        } else if (request.isPublished() != null) {
            post.setIsPublished(request.isPublished());
            post.setStatus(request.isPublished() ? BlogPostStatus.PUBLISHED : BlogPostStatus.DRAFT);
        }

        if (request.publishedAt() != null) {
            post.setPublishedAt(request.publishedAt());
        }

        // SEO Fields
        if (request.seoTitle() != null) {
            post.setSeoTitle(!request.seoTitle().isBlank() ? request.seoTitle().trim() : null);
        }
        if (request.metaDescription() != null) {
            post.setMetaDescription(!request.metaDescription().isBlank() ? request.metaDescription().trim() : null);
        }
        if (request.tags() != null) {
            post.setTags(sanitizeKeywords(request.tags()));
        }
        if (request.focusKeywords() != null) {
            post.setFocusKeywords(sanitizeKeywords(request.focusKeywords()));
        }

        return BlogPostDetailResponse.from(repository.save(post));
    }

    private List<String> sanitizeKeywords(List<String> raw) {
        if (raw == null) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String item : raw) {
            if (item != null) {
                String trimmed = item.trim();
                if (!trimmed.isBlank() && seen.add(trimmed.toLowerCase())) {
                    result.add(trimmed);
                }
            }
        }
        return result;
    }

    @Transactional
    public void delete(UUID id) {
        BlogPost post = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post not found: " + id));

        String publicId = post.getCoverImagePublicId();
        if (publicId != null && !publicId.isBlank()) {
            long otherUsage = repository.countByCoverImagePublicIdAndIdNot(publicId, post.getId());
            if (otherUsage == 0) {
                try {
                    log.info("Deleting blog cover image from Cloudinary upon post deletion (publicId: {})", publicId);
                    mediaService.deleteImage(publicId);
                } catch (Exception e) {
                    log.warn("Failed to delete Cloudinary cover image during blog deletion: {}", e.getMessage());
                }
            } else {
                log.info("Preserving Cloudinary cover image '{}' as it is shared by {} other post(s)", publicId, otherUsage);
            }
        }

        repository.delete(post);
    }
}
