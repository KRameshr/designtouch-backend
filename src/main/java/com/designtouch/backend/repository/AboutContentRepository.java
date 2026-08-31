package com.designtouch.backend.repository;

import com.designtouch.backend.entity.AboutContent;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Singleton table — always fetch/save with id = 1 (see AboutContent entity).
 * The service layer should use findById((short) 1) rather than findAll().
 */
public interface AboutContentRepository extends JpaRepository<AboutContent, Short> {
}
