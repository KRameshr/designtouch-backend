package com.designtouch.backend.repository;

import com.designtouch.backend.entity.PortfolioCategory;
import com.designtouch.backend.entity.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioCategoryRepository extends JpaRepository<PortfolioCategory, UUID> {

    List<PortfolioCategory> findByCategoryTypeAndIsActiveTrueOrderByDisplayOrderAsc(CategoryType categoryType);

    List<PortfolioCategory> findByCategoryTypeOrderByDisplayOrderAsc(CategoryType categoryType);

    List<PortfolioCategory> findAllByOrderByDisplayOrderAsc();

    List<PortfolioCategory> findAllByIsActiveTrueOrderByDisplayOrderAsc();

    Optional<PortfolioCategory> findBySlugAndCategoryType(String slug, CategoryType categoryType);

    boolean existsBySlugAndCategoryType(String slug, CategoryType categoryType);

    boolean existsBySlugAndCategoryTypeAndIdNot(String slug, CategoryType categoryType, UUID id);

    boolean existsByNameIgnoreCaseAndCategoryType(String name, CategoryType categoryType);

    boolean existsByNameIgnoreCaseAndCategoryTypeAndIdNot(String name, CategoryType categoryType, UUID id);
}

