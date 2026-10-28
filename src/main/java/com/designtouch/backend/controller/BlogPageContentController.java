package com.designtouch.backend.controller;

import com.designtouch.backend.dto.BlogPageContentRequest;
import com.designtouch.backend.dto.BlogPageContentResponse;
import com.designtouch.backend.service.BlogPageContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/blog-page")
@RequiredArgsConstructor
public class BlogPageContentController {

    private final BlogPageContentService service;

    @GetMapping
    public BlogPageContentResponse getContent() {
        return service.getBlogPageContent();
    }

    @PutMapping
    public BlogPageContentResponse updateContent(@Valid @RequestBody BlogPageContentRequest request) {
        return service.updateBlogPageContent(request);
    }

    @DeleteMapping("/hero-banner")
    public BlogPageContentResponse deleteHeroBanner() {
        return service.deleteHeroBannerImage();
    }
}
