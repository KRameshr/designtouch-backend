package com.designtouch.backend.repository;

import com.designtouch.backend.entity.PortfolioItem;
import com.designtouch.backend.entity.enums.ContentType;
import com.designtouch.backend.entity.enums.PortfolioCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, UUID> {

    // Home page "Featured Projects" section
    List<PortfolioItem> findByIsFeaturedTrueOrderByDisplayOrderAsc();

    // Portfolio page "ALL WORK" tab, e.g. contentType=VIDEO_EDITING
    List<PortfolioItem> findByContentTypeOrderByDisplayOrderAsc(ContentType contentType);

    // Portfolio page category filter tabs, e.g. contentType=VIDEO_EDITING & category=YOUTUBE
    List<PortfolioItem> findByContentTypeAndCategoryOrderByDisplayOrderAsc(
            ContentType contentType, PortfolioCategory category);

    // "Latest" sort dropdown
    List<PortfolioItem> findByContentTypeOrderByCreatedAtDesc(ContentType contentType);
}
