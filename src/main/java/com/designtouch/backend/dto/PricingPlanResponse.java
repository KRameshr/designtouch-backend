package com.designtouch.backend.dto;

import com.designtouch.backend.entity.PricingPlan;
import com.designtouch.backend.entity.enums.PriceType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PricingPlanResponse(
        UUID id, String planName, String icon, String description, PriceType priceType,
        BigDecimal price, String currency, String unit, List<String> features,
        Boolean isMostPopular, String ctaText, String ctaLink) {

    public static PricingPlanResponse from(PricingPlan p) {
        return new PricingPlanResponse(
                p.getId(), p.getPlanName(), p.getIcon(), p.getDescription(), p.getPriceType(),
                p.getPrice(), p.getCurrency(), p.getUnit(), p.getFeatures(),
                p.getIsMostPopular(), p.getCtaText(), p.getCtaLink());
    }
}
