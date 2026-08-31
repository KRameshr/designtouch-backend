package com.designtouch.backend.repository;

import com.designtouch.backend.entity.BlogPost;
import com.designtouch.backend.entity.enums.BlogCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BlogPostRepository extends JpaRepository<BlogPost, UUID> {

    Optional<BlogPost> findBySlugAndIsPublishedTrue(String slug);

    // Blog listing grid, paginated: GET /api/blogs?page=0&size=9
    Page<BlogPost> findByIsPublishedTrueOrderByPublishedAtDesc(Pageable pageable);

    Page<BlogPost> findByIsPublishedTrueAndCategoryOrderByPublishedAtDesc(
            BlogCategory category, Pageable pageable);

    // "POPULAR POSTS" sidebar — top 5 by view count
    List<BlogPost> findTop5ByIsPublishedTrueOrderByViewCountDesc();

    // Search bar — case-insensitive match on title or excerpt
    @Query("""
            SELECT b FROM BlogPost b
            WHERE b.isPublished = true
            AND (LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(b.excerpt) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY b.publishedAt DESC
            """)
    Page<BlogPost> search(@Param("keyword") String keyword, Pageable pageable);

    // "CATEGORIES" sidebar — category name + post count
    @Query("""
            SELECT b.category AS category, COUNT(b) AS count
            FROM BlogPost b
            WHERE b.isPublished = true
            GROUP BY b.category
            """)
    List<CategoryCount> countByCategory();

    interface CategoryCount {
        BlogCategory getCategory();
        Long getCount();
    }
}
