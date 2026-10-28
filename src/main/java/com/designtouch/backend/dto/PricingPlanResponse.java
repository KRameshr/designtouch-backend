package com.designtouch.backend.dto;

import com.designtouch.backend.entity.PricingPlan;
import com.designtouch.backend.entity.enums.PriceType;
import com.designtouch.backend.entity.enums.PricingServiceCategory;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PricingPlanResponse(
        UUID id,
        PricingServiceCategory serviceCategory,
        String planName,
        String icon,
        String description,
        PriceType priceType,
        BigDecimal price,
        String currency,
        String unit,
        List<String> features,
        Boolean isMostPopular,
        String badgeText,
        String accentColor,
        String ctaText,
        String ctaLink,
        Integer displayOrder,
        Boolean isActive
) {
    public static PricingPlanResponse from(PricingPlan p) {
        if (p == null) return null;
        return new PricingPlanResponse(
                p.getId(),
                p.getServiceCategory() != null ? p.getServiceCategory() : PricingServiceCategory.VIDEO_EDITING,
                p.getPlanName(),
                p.getIcon(),
                p.getDescription(),
                p.getPriceType(),
                p.getPrice(),
                p.getCurrency(),
                p.getUnit(),
                p.getFeatures(),
                p.getIsMostPopular(),
                p.getBadgeText(),
                p.getAccentColor(),
                p.getCtaText(),
                p.getCtaLink(),
                p.getDisplayOrder(),
                p.getIsActive() != null ? p.getIsActive() : true
        );
    }
}
