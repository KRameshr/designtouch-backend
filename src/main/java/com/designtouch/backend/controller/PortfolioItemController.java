package com.designtouch.backend.controller;

import com.designtouch.backend.dto.PortfolioItemRequest;
import com.designtouch.backend.dto.PortfolioItemResponse;
import com.designtouch.backend.entity.PortfolioItem;
import com.designtouch.backend.entity.enums.ContentType;
import com.designtouch.backend.entity.enums.PortfolioCategory;
import com.designtouch.backend.exception.ResourceNotFoundException;
import com.designtouch.backend.repository.PortfolioItemRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioItemController {

    private final PortfolioItemRepository repository;

    // ---------- Public reads ----------

    // Home page "Featured Projects" section
    @GetMapping("/featured")
    public List<PortfolioItemResponse> getFeatured() {
        return repository.findByIsFeaturedTrueOrderByDisplayOrderAsc()
                .stream()
                .map(PortfolioItemResponse::from)
                .toList();
    }

    // Portfolio page:
    //   GET /api/portfolio?contentType=VIDEO_EDITING                       -> "ALL WORK" tab
    //   GET /api/portfolio?contentType=VIDEO_EDITING&category=YOUTUBE      -> category filter tab
    //   GET /api/portfolio?contentType=VIDEO_EDITING&sort=latest           -> "Latest" sort dropdown
    @GetMapping
    public List<PortfolioItemResponse> getByFilters(
            @RequestParam ContentType contentType,
            @RequestParam(required = false) PortfolioCategory category,
            @RequestParam(required = false) String sort) {

        List<PortfolioItem> items;
        if (category != null) {
            items = repository.findByContentTypeAndCategoryOrderByDisplayOrderAsc(contentType, category);
        } else if ("latest".equalsIgnoreCase(sort)) {
            items = repository.findByContentTypeOrderByCreatedAtDesc(contentType);
        } else {
            items = repository.findByContentTypeOrderByDisplayOrderAsc(contentType);
        }

        return items.stream().map(PortfolioItemResponse::from).toList();
    }

    // ---------- Admin writes (require a valid JWT — see SecurityConfig) ----------

    // Admin table view — every item regardless of contentType/featured status
    @GetMapping("/admin/all")
    public List<PortfolioItemResponse> getAllForAdmin() {
        return repository.findAll()
                .stream()
                .map(PortfolioItemResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PortfolioItemResponse create(@Valid @RequestBody PortfolioItemRequest request) {
        PortfolioItem item = PortfolioItem.builder()
                .title(request.title())
                .contentType(request.contentType())
                .category(request.category())
                .subCategory(request.subCategory())
                .description(request.description())
                .duration(request.duration())
                .videoUrl(request.videoUrl())
                .imageUrl(request.imageUrl())
                .thumbnailUrl(request.thumbnailUrl())
                .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
                .isFeatured(request.isFeatured() != null ? request.isFeatured() : false)
                .build();
        return PortfolioItemResponse.from(repository.save(item));
    }

    @PutMapping("/{id}")
    public PortfolioItemResponse update(@PathVariable UUID id, @Valid @RequestBody PortfolioItemRequest request) {
        PortfolioItem item = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio item not found: " + id));

        item.setTitle(request.title());
        item.setContentType(request.contentType());
        item.setCategory(request.category());
        item.setSubCategory(request.subCategory());
        item.setDescription(request.description());
        item.setDuration(request.duration());
        item.setVideoUrl(request.videoUrl());
        item.setImageUrl(request.imageUrl());
        item.setThumbnailUrl(request.thumbnailUrl());
        if (request.displayOrder() != null) item.setDisplayOrder(request.displayOrder());
        if (request.isFeatured() != null) item.setIsFeatured(request.isFeatured());

        return PortfolioItemResponse.from(repository.save(item));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Portfolio item not found: " + id);
        }
        repository.deleteById(id);
    }
}
