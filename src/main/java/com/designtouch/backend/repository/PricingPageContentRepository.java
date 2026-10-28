package com.designtouch.backend.repository;

import com.designtouch.backend.entity.PricingPageContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PricingPageContentRepository extends JpaRepository<PricingPageContent, Short> {
}

