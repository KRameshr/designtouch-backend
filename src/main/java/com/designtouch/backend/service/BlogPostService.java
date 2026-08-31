package com.designtouch.backend.service;

import com.designtouch.backend.dto.BlogPostDetailResponse;
import com.designtouch.backend.dto.BlogPostRequest;
import com.designtouch.backend.dto.BlogPostSummaryResponse;
import com.designtouch.backend.dto.CategoryCountResponse;
import com.designtouch.backend.entity.BlogPost;
import com.designtouch.backend.entity.enums.BlogCategory;
import com.designtouch.backend.exception.ResourceNotFoundException;
import com.designtouch.backend.repository.BlogPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BlogPostService {

    private final BlogPostRepository repository;

    @Transactional(readOnly = true)
    public Page<BlogPostSummaryResponse> getPage(BlogCategory category, Pageable pageable) {
        Page<BlogPost> page = (category != null)
                ? repository.findByIsPublishedTrueAndCategoryOrderByPublishedAtDesc(category, pageable)
                : repository.findByIsPublishedTrueOrderByPublishedAtDesc(pageable);
        return page.map(BlogPostSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<BlogPostSummaryResponse> search(String keyword, Pageable pageable) {
        return repository.search(keyword, pageable).map(BlogPostSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public List<BlogPostSummaryResponse> getPopular() {
        return repository.findTop5ByIsPublishedTrueOrderByViewCountDesc()
                .stream()
                .map(BlogPostSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryCountResponse> getCategoryCounts() {
        return repository.countByCategory()
                .stream()
                .map(c -> new CategoryCountResponse(c.getCategory(), c.getCount()))
                .toList();
    }

    // Fetches the post, converts to DTO (including the lazy author name) while the
    // transaction is still open, and bumps the view counter — all in one place so
    // the controller stays a thin pass-through.
    @Transactional
    public BlogPostDetailResponse getBySlugAndIncrementViews(String slug) {
        BlogPost post = repository.findBySlugAndIsPublishedTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post not found: " + slug));

        post.setViewCount(post.getViewCount() + 1);
        // No explicit save() call needed — this is a managed entity inside an open
        // @Transactional method, so Hibernate's dirty checking flushes the change
        // automatically when the transaction commits.

        return BlogPostDetailResponse.from(post);
    }

    // ---------- Admin ----------

    // Admin table view — includes unpublished drafts, unlike the public endpoints
    @Transactional(readOnly = true)
    public List<BlogPostSummaryResponse> getAllForAdmin() {
        return repository.findAll()
                .stream()
                .map(BlogPostSummaryResponse::from)
                .toList();
    }

    @Transactional
    public BlogPostDetailResponse create(BlogPostRequest request) {
        BlogPost post = BlogPost.builder()
                .title(request.title())
                .slug(request.slug())
                .excerpt(request.excerpt())
                .content(request.content())
                .coverImageUrl(request.coverImageUrl())
                .category(request.category())
                .readingTime(request.readingTime())
                .isPublished(request.isPublished() != null ? request.isPublished() : true)
                .build();
        return BlogPostDetailResponse.from(repository.save(post));
    }

    @Transactional
    public BlogPostDetailResponse update(UUID id, BlogPostRequest request) {
        BlogPost post = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post not found: " + id));

        post.setTitle(request.title());
        post.setSlug(request.slug());
        post.setExcerpt(request.excerpt());
        post.setContent(request.content());
        post.setCoverImageUrl(request.coverImageUrl());
        post.setCategory(request.category());
        post.setReadingTime(request.readingTime());
        if (request.isPublished() != null) post.setIsPublished(request.isPublished());

        return BlogPostDetailResponse.from(repository.save(post));
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Blog post not found: " + id);
        }
        repository.deleteById(id);
    }
}
