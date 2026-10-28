package com.designtouch.backend.repository;

import com.designtouch.backend.entity.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BlogPostRepository extends JpaRepository<BlogPost, UUID> {

    // Public detail lookup by slug: Only PUBLISHED (and reached publication time) or SCHEDULED whose time has arrived
    @Query("""
            SELECT b FROM BlogPost b
            WHERE b.slug = :slug
            AND (
                (b.status = com.designtouch.backend.entity.enums.BlogPostStatus.PUBLISHED AND (b.publishedAt IS NULL OR b.publishedAt <= CURRENT_TIMESTAMP))
                OR (b.status = com.designtouch.backend.entity.enums.BlogPostStatus.SCHEDULED AND b.scheduledFor <= CURRENT_TIMESTAMP)
            )
            """)
    Optional<BlogPost> findPublicBySlug(@Param("slug") String slug);

    default Optional<BlogPost> findBySlugAndIsPublishedTrue(String slug) {
        return findPublicBySlug(slug);
    }

    // Blog listing grid, paginated: GET /api/blogs?page=0&size=9
    @Query("""
            SELECT b FROM BlogPost b
            WHERE (
                (b.status = com.designtouch.backend.entity.enums.BlogPostStatus.PUBLISHED AND (b.publishedAt IS NULL OR b.publishedAt <= CURRENT_TIMESTAMP))
                OR (b.status = com.designtouch.backend.entity.enums.BlogPostStatus.SCHEDULED AND b.scheduledFor <= CURRENT_TIMESTAMP)
            )
            ORDER BY b.publishedAt DESC
            """)
    Page<BlogPost> findPublicPosts(Pageable pageable);

    // Blog listing grid filtered by category
    @Query("""
            SELECT b FROM BlogPost b
            WHERE (
                LOWER(b.category) = LOWER(:category)
                OR (b.categoryRef IS NOT NULL AND LOWER(b.categoryRef.slug) = LOWER(:category))
                OR REPLACE(LOWER(b.category), '_', '-') = LOWER(:category)
                OR REPLACE(LOWER(:category), '-', '_') = LOWER(b.category)
            )
            AND (
                (b.status = com.designtouch.backend.entity.enums.BlogPostStatus.PUBLISHED AND (b.publishedAt IS NULL OR b.publishedAt <= CURRENT_TIMESTAMP))
                OR (b.status = com.designtouch.backend.entity.enums.BlogPostStatus.SCHEDULED AND b.scheduledFor <= CURRENT_TIMESTAMP)
            )
            ORDER BY b.publishedAt DESC
            """)
    Page<BlogPost> findPublicPostsByCategory(@Param("category") String category, Pageable pageable);

    // "POPULAR POSTS" sidebar — top by view count
    @Query("""
            SELECT b FROM BlogPost b
            WHERE (
                (b.status = com.designtouch.backend.entity.enums.BlogPostStatus.PUBLISHED AND (b.publishedAt IS NULL OR b.publishedAt <= CURRENT_TIMESTAMP))
                OR (b.status = com.designtouch.backend.entity.enums.BlogPostStatus.SCHEDULED AND b.scheduledFor <= CURRENT_TIMESTAMP)
            )
            ORDER BY b.viewCount DESC
            """)
    List<BlogPost> findTopPublicPosts(Pageable pageable);

    // Search bar — case-insensitive match on title, excerpt, or SEO title, public posts only
    @Query("""
            SELECT b FROM BlogPost b
            WHERE (
                (b.status = com.designtouch.backend.entity.enums.BlogPostStatus.PUBLISHED AND (b.publishedAt IS NULL OR b.publishedAt <= CURRENT_TIMESTAMP))
                OR (b.status = com.designtouch.backend.entity.enums.BlogPostStatus.SCHEDULED AND b.scheduledFor <= CURRENT_TIMESTAMP)
            )
            AND (LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(b.excerpt) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR (b.seoTitle IS NOT NULL AND LOWER(b.seoTitle) LIKE LOWER(CONCAT('%', :keyword, '%'))))
            ORDER BY b.publishedAt DESC
            """)
    Page<BlogPost> searchPublic(@Param("keyword") String keyword, Pageable pageable);

    // Search bar filtered by specific category
    @Query("""
            SELECT b FROM BlogPost b
            WHERE (
                LOWER(b.category) = LOWER(:category)
                OR (b.categoryRef IS NOT NULL AND LOWER(b.categoryRef.slug) = LOWER(:category))
                OR REPLACE(LOWER(b.category), '_', '-') = LOWER(:category)
                OR REPLACE(LOWER(:category), '-', '_') = LOWER(b.category)
            )
            AND (
                (b.status = com.designtouch.backend.entity.enums.BlogPostStatus.PUBLISHED AND (b.publishedAt IS NULL OR b.publishedAt <= CURRENT_TIMESTAMP))
                OR (b.status = com.designtouch.backend.entity.enums.BlogPostStatus.SCHEDULED AND b.scheduledFor <= CURRENT_TIMESTAMP)
            )
            AND (LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(b.excerpt) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR (b.seoTitle IS NOT NULL AND LOWER(b.seoTitle) LIKE LOWER(CONCAT('%', :keyword, '%'))))
            ORDER BY b.publishedAt DESC
            """)
    Page<BlogPost> searchPublicWithCategory(@Param("keyword") String keyword, @Param("category") String category, Pageable pageable);

    // "CATEGORIES" sidebar — category name + public post count
    @Query("""
            SELECT COALESCE(b.categoryRef.slug, b.category) AS category,
                   COALESCE(b.categoryRef.name, b.category) AS name,
                   COUNT(b) AS count
            FROM BlogPost b
            WHERE (
                (b.status = com.designtouch.backend.entity.enums.BlogPostStatus.PUBLISHED AND (b.publishedAt IS NULL OR b.publishedAt <= CURRENT_TIMESTAMP))
                OR (b.status = com.designtouch.backend.entity.enums.BlogPostStatus.SCHEDULED AND b.scheduledFor <= CURRENT_TIMESTAMP)
            )
            GROUP BY COALESCE(b.categoryRef.slug, b.category), COALESCE(b.categoryRef.name, b.category)
            """)
    List<CategoryCount> countPublicByCategory();

    @Query("""
            SELECT b.categoryRef.id AS categoryId, COUNT(b) AS count
            FROM BlogPost b
            WHERE (
                (b.status = com.designtouch.backend.entity.enums.BlogPostStatus.PUBLISHED AND (b.publishedAt IS NULL OR b.publishedAt <= CURRENT_TIMESTAMP))
                OR (b.status = com.designtouch.backend.entity.enums.BlogPostStatus.SCHEDULED AND b.scheduledFor <= CURRENT_TIMESTAMP)
            )
            AND b.categoryRef IS NOT NULL
            GROUP BY b.categoryRef.id
            """)
    List<CategoryCountById> countPublicByCategoryId();

    // Auto-publishing scheduler: Find all posts scheduled for now or earlier
    @Query("""
            SELECT b FROM BlogPost b
            WHERE b.status = com.designtouch.backend.entity.enums.BlogPostStatus.SCHEDULED
            AND b.scheduledFor IS NOT NULL
            AND b.scheduledFor <= CURRENT_TIMESTAMP
            """)
    List<BlogPost> findPendingScheduledPosts();

    // Admin listing — all posts ordered by publication date descending
    List<BlogPost> findAllByOrderByPublishedAtDesc();

    Optional<BlogPost> findBySlug(String slug);

    Optional<BlogPost> findBySlugIgnoreCase(String slug);

    boolean existsBySlugIgnoreCase(String slug);

    boolean existsBySlugIgnoreCaseAndIdNot(String slug, UUID id);

    long countByCategoryRef_Id(UUID categoryId);

    List<BlogPost> findByCategoryRef_Id(UUID categoryId);

    List<BlogPost> findByCategoryRef_IdOrderByPublishedAtDesc(UUID categoryId);

    long countByCoverImagePublicIdAndIdNot(String coverImagePublicId, UUID excludeId);

    long countByCoverImagePublicId(String coverImagePublicId);

    interface CategoryCount {
        String getCategory();
        String getName();
        Long getCount();
    }

    interface CategoryCountById {
        UUID getCategoryId();
        Long getCount();
    }
}
