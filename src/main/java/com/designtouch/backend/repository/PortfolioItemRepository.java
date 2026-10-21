package com.designtouch.backend.repository;

import com.designtouch.backend.entity.PortfolioItem;
import com.designtouch.backend.entity.enums.ContentType;
import com.designtouch.backend.entity.enums.PortfolioCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, UUID> {

    // Unique slug lookup for public detail routes
    Optional<PortfolioItem> findBySlug(String slug);

    boolean existsBySlug(String slug);

    // Home page "Featured Projects" section
    List<PortfolioItem> findByIsFeaturedTrueOrderByDisplayOrderAsc();

    List<PortfolioItem> findByContentTypeAndIsFeaturedTrueOrderByDisplayOrderAsc(ContentType contentType);

    // Portfolio page "ALL WORK" tab, e.g. contentType=VIDEO_EDITING
    List<PortfolioItem> findByContentTypeOrderByDisplayOrderAsc(ContentType contentType);

    // Legacy portfolio page category filter tab
    List<PortfolioItem> findByContentTypeAndCategoryOrderByDisplayOrderAsc(
            ContentType contentType, PortfolioCategory category);

    // Dynamic database-backed category filter by category ID
    List<PortfolioItem> findByContentTypeAndCategoryRef_IdOrderByDisplayOrderAsc(
            ContentType contentType, UUID categoryId);

    // Dynamic database-backed category filter by category slug
    List<PortfolioItem> findByContentTypeAndCategoryRef_SlugOrderByDisplayOrderAsc(
            ContentType contentType, String slug);

    // "Latest" sort dropdown
    List<PortfolioItem> findByContentTypeOrderByCreatedAtDesc(ContentType contentType);

    // Category usage checks for safe deletion blocker
    long countByCategoryRef_Id(UUID categoryId);

    boolean existsByCategoryRef_Id(UUID categoryId);

    // Asset reference check for safe Cloudinary asset deletion
    boolean existsByImagePublicId(String imagePublicId);
}
