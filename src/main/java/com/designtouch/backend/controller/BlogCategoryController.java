package com.designtouch.backend.controller;

import com.designtouch.backend.dto.BlogCategoryRequest;
import com.designtouch.backend.dto.BlogCategoryResponse;
import com.designtouch.backend.dto.BlogPostSummaryResponse;
import com.designtouch.backend.dto.CategoryArticleMoveRequest;
import com.designtouch.backend.dto.CategoryArticlesDeleteRequest;
import com.designtouch.backend.service.BlogCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/blog-categories")
@RequiredArgsConstructor
public class BlogCategoryController {

    private final BlogCategoryService categoryService;

    // Public list of blog categories (with post counts)
    @GetMapping
    public List<BlogCategoryResponse> getCategories() {
        return categoryService.getCategories();
    }

    @GetMapping("/{id}")
    public BlogCategoryResponse getCategoryById(@PathVariable UUID id) {
        return categoryService.getCategoryById(id);
    }

    // Articles assigned to category (preview before deletion or for admin inspection)
    @GetMapping("/{id}/articles")
    public List<BlogPostSummaryResponse> getArticlesForCategory(@PathVariable UUID id) {
        return categoryService.getArticlesForCategory(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BlogCategoryResponse createCategory(@Valid @RequestBody BlogCategoryRequest request) {
        return categoryService.createCategory(request);
    }

    @PutMapping("/{id}")
    public BlogCategoryResponse updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody BlogCategoryRequest request) {
        return categoryService.updateCategory(id, request);
    }

    // Safe deletion: standard deletion for empty category (fails if articles present)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
    }

    // Option 1: Move articles to another category and delete source category
    @PostMapping("/{id}/delete-and-move")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAndMoveArticles(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryArticleMoveRequest request) {
        categoryService.deleteAndMoveArticles(id, request);
    }

    // Option 2: Delete articles and delete category if empty
    @PostMapping("/{id}/delete-with-articles")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWithArticles(
            @PathVariable UUID id,
            @RequestBody CategoryArticlesDeleteRequest request) {
        categoryService.deleteWithArticles(id, request);
    }
}

