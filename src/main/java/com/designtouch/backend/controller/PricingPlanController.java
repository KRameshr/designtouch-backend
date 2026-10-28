package com.designtouch.backend.controller;

import com.designtouch.backend.dto.PricingPlanRequest;
import com.designtouch.backend.dto.PricingPlanResponse;
import com.designtouch.backend.entity.PricingPlan;
import com.designtouch.backend.entity.enums.PriceType;
import com.designtouch.backend.entity.enums.PricingServiceCategory;
import com.designtouch.backend.exception.ResourceNotFoundException;
import com.designtouch.backend.repository.PricingPlanRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pricing-plans")
@RequiredArgsConstructor
public class PricingPlanController {

    private final PricingPlanRepository repository;

    // Public read: returns active plans for the given price type and optional service category
    // e.g. GET /api/pricing-plans?type=ONE_TIME&category=VIDEO_EDITING
    @GetMapping
    public List<PricingPlanResponse> getByType(
            @RequestParam PriceType type,
            @RequestParam(required = false) PricingServiceCategory category) {
        List<PricingPlan> plans = (category != null)
                ? repository.findByServiceCategoryAndPriceTypeAndIsActiveTrueOrderByDisplayOrderAsc(category, type)
                : repository.findByPriceTypeAndIsActiveTrueOrderByDisplayOrderAsc(type);

        return plans.stream()
                .map(PricingPlanResponse::from)
                .toList();
    }

    // ---------- Admin Endpoints ----------

    @GetMapping("/admin/all")
    public List<PricingPlanResponse> getAllForAdmin(
            @RequestParam(required = false) PriceType type,
            @RequestParam(required = false) PricingServiceCategory category) {
        List<PricingPlan> plans;
        if (category != null && type != null) {
            plans = repository.findByServiceCategoryAndPriceTypeOrderByDisplayOrderAsc(category, type);
        } else if (category != null) {
            plans = repository.findByServiceCategoryOrderByDisplayOrderAsc(category);
        } else if (type != null) {
            plans = repository.findByPriceTypeOrderByDisplayOrderAsc(type);
        } else {
            plans = repository.findAllByOrderByDisplayOrderAsc();
        }

        return plans.stream()
                .map(PricingPlanResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PricingPlanResponse create(@Valid @RequestBody PricingPlanRequest request) {
        PricingPlan plan = PricingPlan.builder()
                .serviceCategory(request.serviceCategory() != null ? request.serviceCategory() : PricingServiceCategory.VIDEO_EDITING)
                .planName(request.planName())
                .icon(request.icon())
                .description(request.description())
                .priceType(request.priceType())
                .price(request.price())
                .currency(request.currency() != null ? request.currency() : "₹")
                .unit(request.unit())
                .features(request.features())
                .isMostPopular(request.isMostPopular() != null ? request.isMostPopular() : false)
                .badgeText(request.badgeText())
                .accentColor(request.accentColor())
                .ctaText(request.ctaText() != null ? request.ctaText() : "GET STARTED")
                .ctaLink(request.ctaLink())
                .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
                .isActive(request.isActive() != null ? request.isActive() : true)
                .build();

        return PricingPlanResponse.from(repository.save(plan));
    }

    @PutMapping("/{id}")
    public PricingPlanResponse update(@PathVariable UUID id, @Valid @RequestBody PricingPlanRequest request) {
        PricingPlan plan = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing plan not found: " + id));

        if (request.serviceCategory() != null) plan.setServiceCategory(request.serviceCategory());
        plan.setPlanName(request.planName());
        plan.setIcon(request.icon());
        plan.setDescription(request.description());
        plan.setPriceType(request.priceType());
        plan.setPrice(request.price());
        if (request.currency() != null) plan.setCurrency(request.currency());
        plan.setUnit(request.unit());
        plan.setFeatures(request.features());
        if (request.isMostPopular() != null) plan.setIsMostPopular(request.isMostPopular());
        plan.setBadgeText(request.badgeText());
        plan.setAccentColor(request.accentColor());
        if (request.ctaText() != null) plan.setCtaText(request.ctaText());
        plan.setCtaLink(request.ctaLink());
        if (request.displayOrder() != null) plan.setDisplayOrder(request.displayOrder());
        if (request.isActive() != null) plan.setIsActive(request.isActive());

        return PricingPlanResponse.from(repository.save(plan));
    }

    @PatchMapping("/{id}/toggle-active")
    public PricingPlanResponse toggleActive(@PathVariable UUID id) {
        PricingPlan plan = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing plan not found: " + id));
        boolean current = plan.getIsActive() != null ? plan.getIsActive() : true;
        plan.setIsActive(!current);
        return PricingPlanResponse.from(repository.save(plan));
    }

    @PutMapping("/reorder")
    public List<PricingPlanResponse> reorder(@RequestBody List<UUID> orderedIds) {
        if (orderedIds != null) {
            for (int i = 0; i < orderedIds.size(); i++) {
                UUID id = orderedIds.get(i);
                int order = i + 1;
                repository.findById(id).ifPresent(p -> {
                    p.setDisplayOrder(order);
                    repository.save(p);
                });
            }
        }
        return repository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(PricingPlanResponse::from)
                .toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Pricing plan not found: " + id);
        }
        repository.deleteById(id);
    }
}
