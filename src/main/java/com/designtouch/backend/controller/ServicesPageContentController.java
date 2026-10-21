package com.designtouch.backend.controller;

import com.designtouch.backend.dto.ServicesPageContentRequest;
import com.designtouch.backend.dto.ServicesPageContentResponse;
import com.designtouch.backend.service.ServicesPageContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/services-page")
@RequiredArgsConstructor
public class ServicesPageContentController {

    private final ServicesPageContentService service;

    @GetMapping
    public ServicesPageContentResponse getContent() {
        return service.getServicesPageContent();
    }

    @PutMapping
    public ServicesPageContentResponse updateContent(@Valid @RequestBody ServicesPageContentRequest request) {
        return service.updateServicesPageContent(request);
    }

    @DeleteMapping("/hero-banner")
    public ServicesPageContentResponse deleteHeroBanner() {
        return service.deleteHeroBannerImage();
    }
}

