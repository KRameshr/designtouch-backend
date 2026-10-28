package com.designtouch.backend.repository;

import com.designtouch.backend.entity.PricingPlan;
import com.designtouch.backend.entity.enums.PriceType;
import com.designtouch.backend.entity.enums.PricingServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PricingPlanRepository extends JpaRepository<PricingPlan, UUID> {
    List<PricingPlan> findByPriceTypeAndIsActiveTrueOrderByDisplayOrderAsc(PriceType priceType);
    List<PricingPlan> findByPriceTypeOrderByDisplayOrderAsc(PriceType priceType);
    List<PricingPlan> findAllByOrderByDisplayOrderAsc();

    List<PricingPlan> findByServiceCategoryAndPriceTypeAndIsActiveTrueOrderByDisplayOrderAsc(
            PricingServiceCategory serviceCategory, PriceType priceType);

    List<PricingPlan> findByServiceCategoryAndPriceTypeOrderByDisplayOrderAsc(
            PricingServiceCategory serviceCategory, PriceType priceType);

    List<PricingPlan> findByServiceCategoryOrderByDisplayOrderAsc(
            PricingServiceCategory serviceCategory);

    List<PricingPlan> findByServiceCategoryAndIsActiveTrueOrderByDisplayOrderAsc(
            PricingServiceCategory serviceCategory);
}
