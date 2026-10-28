package com.designtouch.backend.controller;

import com.designtouch.backend.dto.PricingHeroBannerRequest;
import com.designtouch.backend.dto.PricingHeroBannerResponse;
import com.designtouch.backend.dto.PricingPageContentRequest;
import com.designtouch.backend.dto.PricingPageContentResponse;
import com.designtouch.backend.service.PricingPageContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pricing-page")
@RequiredArgsConstructor
public class PricingPageContentController {

    private final PricingPageContentService service;

    @GetMapping
    public PricingPageContentResponse getContent() {
        return service.getPricingPageContent();
    }

    @PutMapping
    public PricingPageContentResponse updateContent(@Valid @RequestBody PricingPageContentRequest request) {
        return service.updatePricingPageContent(request);
    }

    @DeleteMapping("/hero-banner")
    public PricingPageContentResponse deleteHeroBanner() {
        return service.deleteHeroBannerImage();
    }

    // ---------- Multi-Banner Endpoints (Max 5) ----------

    @GetMapping("/banners")
    public List<PricingHeroBannerResponse> getActiveBanners() {
        return service.getActiveHeroBanners();
    }

    @GetMapping("/banners/admin/all")
    public List<PricingHeroBannerResponse> getAllBannersAdmin() {
        return service.getAllHeroBannersAdmin();
    }

    @PostMapping("/banners")
    @ResponseStatus(HttpStatus.CREATED)
    public PricingHeroBannerResponse createBanner(@Valid @RequestBody PricingHeroBannerRequest request) {
        return service.createHeroBanner(request);
    }

    @PutMapping("/banners/{id}")
    public PricingHeroBannerResponse updateBanner(
            @PathVariable UUID id,
            @Valid @RequestBody PricingHeroBannerRequest request
    ) {
        return service.updateHeroBanner(id, request);
    }

    @PatchMapping("/banners/{id}/toggle-active")
    public PricingHeroBannerResponse toggleBannerActive(@PathVariable UUID id) {
        return service.toggleHeroBannerActive(id);
    }

    @PutMapping("/banners/reorder")
    public List<PricingHeroBannerResponse> reorderBanners(@RequestBody List<UUID> orderedIds) {
        return service.reorderHeroBanners(orderedIds);
    }

    @DeleteMapping("/banners/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBanner(@PathVariable UUID id) {
        service.deleteHeroBanner(id);
    }
}
