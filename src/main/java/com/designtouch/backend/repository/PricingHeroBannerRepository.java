package com.designtouch.backend.repository;

import com.designtouch.backend.entity.PricingHeroBanner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PricingHeroBannerRepository extends JpaRepository<PricingHeroBanner, UUID> {

    List<PricingHeroBanner> findAllByIsActiveTrueOrderByDisplayOrderAscCreatedAtAsc();

    List<PricingHeroBanner> findAllByOrderByDisplayOrderAscCreatedAtAsc();

    boolean existsByImagePublicId(String imagePublicId);

    boolean existsByImagePublicIdAndIdNot(String imagePublicId, UUID id);
}

