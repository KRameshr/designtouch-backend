package com.designtouch.backend.controller;

import com.designtouch.backend.dto.BlogPostDetailResponse;
import com.designtouch.backend.dto.BlogPostRequest;
import com.designtouch.backend.dto.BlogPostSummaryResponse;
import com.designtouch.backend.dto.CategoryCountResponse;
import com.designtouch.backend.service.BlogPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class BlogPostController {

    private final BlogPostService service;

    // ---------- Public reads ----------

    // GET /api/blogs?page=0&size=9&category=video-editing (category optional)
    @GetMapping
    public Page<BlogPostSummaryResponse> getAll(
            @RequestParam(required = false) String category,
            @PageableDefault(size = 9) Pageable pageable) {
        return service.getPage(category, pageable);
    }

    // GET /api/blogs/search?q=video+editing&category=video-editing&page=0&size=9
    @GetMapping("/search")
    public Page<BlogPostSummaryResponse> search(
            @RequestParam String q,
            @RequestParam(required = false) String category,
            @PageableDefault(size = 9) Pageable pageable) {
        return service.search(q, category, pageable);
    }

    // "POPULAR POSTS" sidebar
    @GetMapping("/popular")
    public List<BlogPostSummaryResponse> getPopular() {
        return service.getPopular();
    }

    // "CATEGORIES" sidebar with counts
    @GetMapping("/categories")
    public List<CategoryCountResponse> getCategoryCounts() {
        return service.getCategoryCounts();
    }

    // ---------- Admin (requires JWT — see SecurityConfig) ----------

    // Admin table view — includes unpublished drafts
    @GetMapping("/admin/all")
    public List<BlogPostSummaryResponse> getAllForAdmin() {
        return service.getAllForAdmin();
    }

    // Admin detail view by ID — includes full content and private/draft posts
    @GetMapping("/admin/{id}")
    public BlogPostDetailResponse getByIdForAdmin(@PathVariable UUID id) {
        return service.getByIdForAdmin(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BlogPostDetailResponse create(@Valid @RequestBody BlogPostRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public BlogPostDetailResponse update(@PathVariable UUID id, @Valid @RequestBody BlogPostRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    // ---------- Public detail (must stay LAST — /{slug} would otherwise
    // greedily match /admin/all, /search, /popular, /categories above) ----------

    // Single post detail page — also increments the view counter
    @GetMapping("/{slug}")
    public BlogPostDetailResponse getBySlug(@PathVariable String slug) {
        return service.getBySlugAndIncrementViews(slug);
    }
}
