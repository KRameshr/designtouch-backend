package com.designtouch.backend.repository;

import com.designtouch.backend.entity.ProcessStep;
import com.designtouch.backend.entity.enums.PageType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProcessStepRepository extends JpaRepository<ProcessStep, UUID> {

    // Ordered by stepNumber (not displayOrder) so "1. DISCOVER", "2. PLAN"... stay in sequence
    List<ProcessStep> findByPageTypeOrderByStepNumberAsc(PageType pageType);
}
