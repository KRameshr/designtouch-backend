package com.designtouch.backend.repository;

import com.designtouch.backend.entity.ServicesHeroBanner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ServicesHeroBannerRepository extends JpaRepository<ServicesHeroBanner, UUID> {

    List<ServicesHeroBanner> findAllByIsActiveTrueOrderByDisplayOrderAscCreatedAtAsc();

    List<ServicesHeroBanner> findAllByOrderByDisplayOrderAscCreatedAtAsc();

    boolean existsByImagePublicId(String imagePublicId);

    boolean existsByImagePublicIdAndIdNot(String imagePublicId, UUID id);
}

