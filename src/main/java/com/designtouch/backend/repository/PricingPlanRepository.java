package com.designtouch.backend.repository;

import com.designtouch.backend.entity.PricingPlan;
import com.designtouch.backend.entity.enums.PriceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PricingPlanRepository extends JpaRepository<PricingPlan, UUID> {

    // "ONE-TIME PROJECT" vs "MONTHLY PACKAGES" toggle
    List<PricingPlan> findByPriceTypeOrderByDisplayOrderAsc(PriceType priceType);
}
