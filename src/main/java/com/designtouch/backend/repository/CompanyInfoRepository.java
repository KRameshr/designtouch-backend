package com.designtouch.backend.repository;

import com.designtouch.backend.entity.CompanyInfo;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Singleton table — always fetch/save with id = 1 (see CompanyInfo entity).
 * The service layer should use findById((short) 1) rather than findAll().
 */
public interface CompanyInfoRepository extends JpaRepository<CompanyInfo, Short> {
}
