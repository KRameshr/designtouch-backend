-- ==========================================================
-- V11__create_portfolio_category.sql
-- Portfolio category database CMS: separate video & thumbnail categories
-- ==========================================================

-- 0. Safely rename the legacy enum type if it still exists
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_type WHERE typname = 'portfolio_category' AND typtype = 'e') THEN
        ALTER TYPE portfolio_category RENAME TO portfolio_category_enum;
    END IF;
END $$;

-- 1. Create portfolio_category table
CREATE TABLE IF NOT EXISTS portfolio_category (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100) NOT NULL,
    slug            VARCHAR(100) NOT NULL,
    category_type   VARCHAR(50) NOT NULL,
    display_order   INTEGER NOT NULL DEFAULT 0,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_portfolio_category_type CHECK (category_type IN ('VIDEO_EDITING', 'THUMBNAIL_DESIGN')),
    CONSTRAINT uq_portfolio_category_slug_type UNIQUE (slug, category_type)
);

CREATE INDEX IF NOT EXISTS idx_portfolio_category_type_order ON portfolio_category(category_type, display_order);

-- 2. Seed initial Video Editing categories
INSERT INTO portfolio_category (name, slug, category_type, display_order, is_active) VALUES
    ('YouTube', 'youtube', 'VIDEO_EDITING', 1, TRUE),
    ('Instagram Reels', 'instagram-reels', 'VIDEO_EDITING', 2, TRUE),
    ('Commercial', 'commercial', 'VIDEO_EDITING', 3, TRUE),
    ('Motion Graphics', 'motion-graphics', 'VIDEO_EDITING', 4, TRUE),
    ('Corporate', 'corporate', 'VIDEO_EDITING', 5, TRUE),
    ('Real Estate', 'real-estate', 'VIDEO_EDITING', 6, TRUE)
ON CONFLICT (slug, category_type) DO NOTHING;

-- 3. Seed initial Thumbnail Design categories
INSERT INTO portfolio_category (name, slug, category_type, display_order, is_active) VALUES
    ('Motivation Content', 'motivation-content', 'THUMBNAIL_DESIGN', 1, TRUE),
    ('Finance Content', 'finance-content', 'THUMBNAIL_DESIGN', 2, TRUE),
    ('Tech Content', 'tech-content', 'THUMBNAIL_DESIGN', 3, TRUE),
    ('Tutorial Content', 'tutorial-content', 'THUMBNAIL_DESIGN', 4, TRUE),
    ('Entertainment Content', 'entertainment-content', 'THUMBNAIL_DESIGN', 5, TRUE)
ON CONFLICT (slug, category_type) DO NOTHING;

-- 4. Add category_id to portfolio_item
ALTER TABLE portfolio_item
ADD COLUMN IF NOT EXISTS category_id UUID REFERENCES portfolio_category(id) ON DELETE RESTRICT;

CREATE INDEX IF NOT EXISTS idx_portfolio_item_category_id ON portfolio_item(category_id);

-- 5. Backfill existing Video Editing projects
UPDATE portfolio_item pi
SET category_id = pc.id
FROM portfolio_category pc
WHERE pi.category_id IS NULL
  AND pc.category_type = 'VIDEO_EDITING'
  AND (
    (pi.category::text = 'YOUTUBE' AND pc.slug = 'youtube') OR
    (pi.category::text = 'INSTAGRAM_REELS' AND pc.slug = 'instagram-reels') OR
    (pi.category::text = 'COMMERCIAL' AND pc.slug = 'commercial') OR
    (pi.category::text = 'MOTION_GRAPHICS' AND pc.slug = 'motion-graphics') OR
    (pi.category::text = 'CORPORATE' AND pc.slug = 'corporate') OR
    (pi.category::text = 'REAL_ESTATE' AND pc.slug = 'real-estate')
  );

-- 6. Backfill existing Thumbnail Design projects
UPDATE portfolio_item pi
SET category_id = pc.id
FROM portfolio_category pc
WHERE pi.category_id IS NULL
  AND pc.category_type = 'THUMBNAIL_DESIGN'
  AND (
    (pi.sub_category ILIKE '%Motivation%' AND pc.slug = 'motivation-content') OR
    (pi.sub_category ILIKE '%Finance%' AND pc.slug = 'finance-content') OR
    (pi.sub_category ILIKE '%Tech%' AND pc.slug = 'tech-content') OR
    (pi.sub_category ILIKE '%Tutorial%' AND pc.slug = 'tutorial-content') OR
    (pi.sub_category ILIKE '%Entertainment%' AND pc.slug = 'entertainment-content')
  );
