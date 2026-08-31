package com.designtouch.backend.controller;

import com.designtouch.backend.dto.PricingPlanResponse;
import com.designtouch.backend.entity.enums.PriceType;
import com.designtouch.backend.repository.PricingPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pricing-plans")
@RequiredArgsConstructor
public class PricingPlanController {

    private final PricingPlanRepository repository;

    // "ONE-TIME PROJECT" vs "MONTHLY PACKAGES" toggle
    // e.g. GET /api/pricing-plans?type=ONE_TIME
    @GetMapping
    public List<PricingPlanResponse> getByType(@RequestParam PriceType type) {
        return repository.findByPriceTypeOrderByDisplayOrderAsc(type)
                .stream()
                .map(PricingPlanResponse::from)
                .toList();
    }
}
