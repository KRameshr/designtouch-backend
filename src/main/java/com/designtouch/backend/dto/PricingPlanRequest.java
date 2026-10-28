package com.designtouch.backend.dto;

import com.designtouch.backend.entity.enums.PriceType;
import com.designtouch.backend.entity.enums.PricingServiceCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record PricingPlanRequest(
        PricingServiceCategory serviceCategory,

        @NotBlank @Size(max = 100)
        String planName,

        @Size(max = 100)
        String icon,

        String description,

        @NotNull
        PriceType priceType,

        // Null = custom pricing (Enterprise)
        BigDecimal price,

        @Size(max = 5)
        String currency,

        @Size(max = 30)
        String unit,

        List<String> features,

        Boolean isMostPopular,

        @Size(max = 50)
        String badgeText,

        @Size(max = 50)
        String accentColor,

        @Size(max = 50)
        String ctaText,

        @Size(max = 200)
        String ctaLink,

        Integer displayOrder,

        Boolean isActive
) {
}

