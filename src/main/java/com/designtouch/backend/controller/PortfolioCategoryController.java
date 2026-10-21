package com.designtouch.backend.controller;

import com.designtouch.backend.dto.PortfolioCategoryRequest;
import com.designtouch.backend.dto.PortfolioCategoryResponse;
import com.designtouch.backend.entity.enums.CategoryType;
import com.designtouch.backend.service.PortfolioCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/portfolio-categories")
@RequiredArgsConstructor
public class PortfolioCategoryController {

    private final PortfolioCategoryService categoryService;

    @GetMapping
    public List<PortfolioCategoryResponse> getCategories(
            @RequestParam(required = false) CategoryType type,
            @RequestParam(required = false, defaultValue = "false") boolean all) {
        return categoryService.getCategories(type, all);
    }

    @GetMapping("/{id}")
    public PortfolioCategoryResponse getCategoryById(@PathVariable UUID id) {
        return categoryService.getCategoryById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PortfolioCategoryResponse createCategory(@Valid @RequestBody PortfolioCategoryRequest request) {
        return categoryService.createCategory(request);
    }

    @PutMapping("/{id}")
    public PortfolioCategoryResponse updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody PortfolioCategoryRequest request) {
        return categoryService.updateCategory(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
    }
}

