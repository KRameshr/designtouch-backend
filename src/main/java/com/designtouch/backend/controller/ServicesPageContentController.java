package com.designtouch.backend.controller;

import com.designtouch.backend.dto.ServicesHeroBannerRequest;
import com.designtouch.backend.dto.ServicesHeroBannerResponse;
import com.designtouch.backend.dto.ServicesPageContentRequest;
import com.designtouch.backend.dto.ServicesPageContentResponse;
import com.designtouch.backend.service.ServicesPageContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    // ---------- Multi-Banner Public & Admin Endpoints (Max 5) ----------

    @GetMapping("/banners")
    public List<ServicesHeroBannerResponse> getActiveBanners() {
        return service.getActiveHeroBanners();
    }

    @GetMapping("/banners/admin/all")
    public List<ServicesHeroBannerResponse> getAllBannersAdmin() {
        return service.getAllHeroBannersAdmin();
    }

    @PostMapping("/banners")
    @ResponseStatus(HttpStatus.CREATED)
    public ServicesHeroBannerResponse createBanner(@Valid @RequestBody ServicesHeroBannerRequest request) {
        return service.createHeroBanner(request);
    }

    @PutMapping("/banners/{id}")
    public ServicesHeroBannerResponse updateBanner(
            @PathVariable UUID id,
            @Valid @RequestBody ServicesHeroBannerRequest request
    ) {
        return service.updateHeroBanner(id, request);
    }

    @PatchMapping("/banners/{id}/toggle-active")
    public ServicesHeroBannerResponse toggleBannerActive(@PathVariable UUID id) {
        return service.toggleHeroBannerActive(id);
    }

    @PutMapping("/banners/reorder")
    public List<ServicesHeroBannerResponse> reorderBanners(@RequestBody List<UUID> orderedIds) {
        return service.reorderHeroBanners(orderedIds);
    }

    @DeleteMapping("/banners/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBanner(@PathVariable UUID id) {
        service.deleteHeroBanner(id);
    }
}
