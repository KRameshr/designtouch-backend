package com.designtouch.backend.repository;

import com.designtouch.backend.entity.BlogCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BlogCategoryRepository extends JpaRepository<BlogCategory, UUID> {

    List<BlogCategory> findAllByOrderByDisplayOrderAsc();

    Optional<BlogCategory> findBySlug(String slug);

    Optional<BlogCategory> findBySlugIgnoreCase(String slug);

    Optional<BlogCategory> findByNameIgnoreCase(String name);

    boolean existsBySlugIgnoreCase(String slug);

    boolean existsBySlugIgnoreCaseAndIdNot(String slug, UUID id);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
}

