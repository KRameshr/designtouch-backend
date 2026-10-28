package com.designtouch.backend.service;

import com.designtouch.backend.dto.BlogCategoryRequest;
import com.designtouch.backend.dto.BlogCategoryResponse;
import com.designtouch.backend.dto.BlogPostSummaryResponse;
import com.designtouch.backend.dto.CategoryArticleMoveRequest;
import com.designtouch.backend.dto.CategoryArticlesDeleteRequest;
import com.designtouch.backend.entity.BlogCategory;
import com.designtouch.backend.entity.BlogPost;
import com.designtouch.backend.exception.ResourceNotFoundException;
import com.designtouch.backend.repository.BlogCategoryRepository;
import com.designtouch.backend.repository.BlogPostRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BlogCategoryService {

    private static final Logger log = LoggerFactory.getLogger(BlogCategoryService.class);

    private final BlogCategoryRepository categoryRepository;
    private final BlogPostRepository blogPostRepository;
    private final MediaService mediaService;

    @Transactional(readOnly = true)
    public List<BlogCategoryResponse> getCategories() {
        List<BlogCategory> categories = categoryRepository.findAllByOrderByDisplayOrderAsc();
        return categories.stream()
                .map(cat -> {
                    long count = blogPostRepository.countByCategoryRef_Id(cat.getId());
                    return BlogCategoryResponse.from(cat, count);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public BlogCategoryResponse getCategoryById(UUID id) {
        BlogCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog category not found: " + id));
        long count = blogPostRepository.countByCategoryRef_Id(id);
        return BlogCategoryResponse.from(category, count);
    }

    @Transactional(readOnly = true)
    public List<BlogPostSummaryResponse> getArticlesForCategory(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Blog category not found: " + id);
        }
        return blogPostRepository.findByCategoryRef_IdOrderByPublishedAtDesc(id)
                .stream()
                .map(BlogPostSummaryResponse::from)
                .toList();
    }

    @Transactional
    public BlogCategoryResponse createCategory(BlogCategoryRequest request) {
        String name = request.name().trim();
        String slug = (request.slug() != null && !request.slug().isBlank())
                ? slugify(request.slug())
                : slugify(name);

        if (categoryRepository.existsBySlugIgnoreCase(slug)) {
            throw new IllegalArgumentException("A category with slug '" + slug + "' already exists.");
        }

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("A category named '" + name + "' already exists.");
        }

        Integer displayOrder = request.displayOrder();
        if (displayOrder == null) {
            displayOrder = (int) categoryRepository.count() + 1;
        }

        BlogCategory category = BlogCategory.builder()
                .name(name)
                .slug(slug)
                .description(request.description() != null ? request.description().trim() : null)
                .displayOrder(displayOrder)
                .build();

        BlogCategory saved = categoryRepository.save(category);
        return BlogCategoryResponse.from(saved, 0L);
    }

    @Transactional
    public BlogCategoryResponse updateCategory(UUID id, BlogCategoryRequest request) {
        BlogCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog category not found: " + id));

        String name = request.name().trim();
        String slug = (request.slug() != null && !request.slug().isBlank())
                ? slugify(request.slug())
                : slugify(name);

        if (categoryRepository.existsBySlugIgnoreCaseAndIdNot(slug, id)) {
            throw new IllegalArgumentException("A category with slug '" + slug + "' already exists.");
        }

        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new IllegalArgumentException("A category named '" + name + "' already exists.");
        }

        String oldSlug = category.getSlug();
        category.setName(name);
        category.setSlug(slug);

        if (request.description() != null) {
            category.setDescription(request.description().trim());
        }
        if (request.displayOrder() != null) {
            category.setDisplayOrder(request.displayOrder());
        }

        BlogCategory updated = categoryRepository.save(category);

        // If the slug changed, synchronize denormalized category column on associated posts
        if (!oldSlug.equalsIgnoreCase(slug)) {
            List<BlogPost> posts = blogPostRepository.findByCategoryRef_Id(id);
            for (BlogPost post : posts) {
                post.setCategory(slug);
            }
            blogPostRepository.saveAll(posts);
            log.info("Synchronized new category slug '{}' on {} blog post(s)", slug, posts.size());
        }

        long count = blogPostRepository.countByCategoryRef_Id(id);
        return BlogCategoryResponse.from(updated, count);
    }

    @Transactional
    public void deleteCategory(UUID id) {
        BlogCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog category not found: " + id));

        long count = blogPostRepository.countByCategoryRef_Id(id);
        if (count > 0) {
            throw new IllegalStateException(
                    "This category contains " + count + " article" + (count == 1 ? "" : "s") +
                    ". Please move the articles to another category or delete them first."
            );
        }

        categoryRepository.delete(category);
        log.info("Deleted empty blog category '{}' (id: {})", category.getName(), id);
    }

    @Transactional
    public void deleteAndMoveArticles(UUID sourceId, CategoryArticleMoveRequest request) {
        if (request.destinationCategoryId() == null) {
            throw new IllegalArgumentException("Destination category ID must be provided.");
        }
        if (sourceId.equals(request.destinationCategoryId())) {
            throw new IllegalArgumentException("Destination category must be different from source category.");
        }

        BlogCategory sourceCategory = categoryRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Source blog category not found: " + sourceId));

        BlogCategory destinationCategory = categoryRepository.findById(request.destinationCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination blog category not found: " + request.destinationCategoryId()));

        List<BlogPost> posts = blogPostRepository.findByCategoryRef_Id(sourceId);
        log.info("Moving {} articles from category '{}' to '{}'", posts.size(), sourceCategory.getName(), destinationCategory.getName());

        for (BlogPost post : posts) {
            post.setCategoryRef(destinationCategory);
            post.setCategory(destinationCategory.getSlug());
        }
        blogPostRepository.saveAll(posts);

        categoryRepository.delete(sourceCategory);
        log.info("Deleted source category '{}' after successfully reassigning all articles", sourceCategory.getName());
    }

    @Transactional
    public void deleteWithArticles(UUID categoryId, CategoryArticlesDeleteRequest request) {
        BlogCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog category not found: " + categoryId));

        List<BlogPost> allPosts = blogPostRepository.findByCategoryRef_Id(categoryId);

        List<BlogPost> postsToDelete;
        if (Boolean.TRUE.equals(request.deleteAll())) {
            postsToDelete = allPosts;
        } else if (request.articleIds() != null && !request.articleIds().isEmpty()) {
            postsToDelete = allPosts.stream()
                    .filter(p -> request.articleIds().contains(p.getId()))
                    .toList();
        } else {
            throw new IllegalArgumentException("Either deleteAll must be true or a non-empty list of articleIds must be provided.");
        }

        log.info("Deleting {} articles from category '{}'", postsToDelete.size(), category.getName());

        for (BlogPost post : postsToDelete) {
            deletePostSafely(post);
        }

        // Check if any articles remain in this category
        long remainingCount = blogPostRepository.countByCategoryRef_Id(categoryId);
        if (remainingCount == 0) {
            categoryRepository.delete(category);
            log.info("Deleted blog category '{}' after clearing all articles", category.getName());
        } else {
            log.info("Blog category '{}' retained {} remaining article(s)", category.getName(), remainingCount);
        }
    }

    private void deletePostSafely(BlogPost post) {
        String publicId = post.getCoverImagePublicId();
        if (publicId != null && !publicId.isBlank()) {
            // Verify if any other post uses this Cloudinary public ID
            long otherUsage = blogPostRepository.countByCoverImagePublicIdAndIdNot(publicId, post.getId());
            if (otherUsage == 0) {
                try {
                    log.info("Deleting cover image from Cloudinary (publicId: {})", publicId);
                    mediaService.deleteImage(publicId);
                } catch (Exception e) {
                    log.warn("Failed to delete Cloudinary cover image during safe post deletion: {}", e.getMessage());
                }
            } else {
                log.info("Retaining Cloudinary image '{}' as it is shared by {} other post(s)", publicId, otherUsage);
            }
        }
        blogPostRepository.delete(post);
    }

    public static String slugify(String input) {
        if (input == null) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }
}

