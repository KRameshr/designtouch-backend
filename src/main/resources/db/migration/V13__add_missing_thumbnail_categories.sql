-- ==========================================================
-- V13__add_missing_thumbnail_categories.sql
-- Add missing thumbnail categories and backfill orphaned thumbnail items
-- ==========================================================

-- 1. Insert missing categories for initial seed items
INSERT INTO portfolio_category (name, slug, category_type, display_order, is_active) VALUES
    ('Lifestyle Content', 'lifestyle-content', 'THUMBNAIL_DESIGN', 6, TRUE),
    ('Vlog Content', 'vlog-content', 'THUMBNAIL_DESIGN', 7, TRUE),
    ('Education Content', 'education-content', 'THUMBNAIL_DESIGN', 8, TRUE),
    ('Gaming Content', 'gaming-content', 'THUMBNAIL_DESIGN', 9, TRUE)
ON CONFLICT (slug, category_type) DO NOTHING;

-- 2. Backfill category_id for orphaned Thumbnail Design projects
UPDATE portfolio_item pi
SET category_id = pc.id
FROM portfolio_category pc
WHERE pi.category_id IS NULL
  AND pc.category_type = 'THUMBNAIL_DESIGN'
  AND (
    (pi.sub_category ILIKE '%Lifestyle%' AND pc.slug = 'lifestyle-content') OR
    (pi.sub_category ILIKE '%Vlog%' AND pc.slug = 'vlog-content') OR
    (pi.sub_category ILIKE '%Education%' AND pc.slug = 'education-content') OR
    (pi.sub_category ILIKE '%Gaming%' AND pc.slug = 'gaming-content')
  );

-- 3. Also backfill any remaining projects where sub_category matches category name or slug exactly
UPDATE portfolio_item pi
SET category_id = pc.id
FROM portfolio_category pc
WHERE pi.category_id IS NULL
  AND pc.category_type = pi.content_type::text
  AND (
    LOWER(TRIM(pi.sub_category)) = LOWER(TRIM(pc.name)) OR
    LOWER(TRIM(pi.sub_category)) = LOWER(TRIM(pc.slug))
  );

