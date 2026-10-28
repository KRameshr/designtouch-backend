-- ==========================================================
-- V30__create_blog_category_table.sql
-- Complete dynamic blog category management with safe article foreign keys
-- ==========================================================

-- 1. Safely alter blog_post.category from Postgres enum to VARCHAR(100) and rename legacy enum type
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_type WHERE typname = 'blog_category' AND typtype = 'e') THEN
        ALTER TABLE blog_post ALTER COLUMN category TYPE VARCHAR(100) USING category::text;
        ALTER TABLE blog_post ALTER COLUMN category DROP NOT NULL;
        ALTER TYPE blog_category RENAME TO blog_category_enum;
    END IF;
END $$;

-- 2. Create blog_category table
CREATE TABLE IF NOT EXISTS blog_category (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100) NOT NULL,
    slug            VARCHAR(100) NOT NULL UNIQUE,
    description     TEXT,
    display_order   INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_blog_category_slug ON blog_category(slug);
CREATE INDEX IF NOT EXISTS idx_blog_category_order ON blog_category(display_order);

-- 3. Seed the 5 canonical blog categories
INSERT INTO blog_category (name, slug, display_order) VALUES
    ('Video Editing', 'VIDEO_EDITING', 1),
    ('Motion Graphics', 'MOTION_GRAPHICS', 2),
    ('Content Creation', 'CONTENT_CREATION', 3),
    ('YouTube', 'YOUTUBE', 4),
    ('Business', 'BUSINESS', 5)
ON CONFLICT (slug) DO NOTHING;

-- 4. Add category_id foreign key column to blog_post
ALTER TABLE blog_post
    ADD COLUMN IF NOT EXISTS category_id UUID REFERENCES blog_category(id) ON DELETE RESTRICT;

CREATE INDEX IF NOT EXISTS idx_blog_post_category_id ON blog_post(category_id);

-- 5. Backfill existing blog posts with category_id
UPDATE blog_post bp
SET category_id = bc.id
FROM blog_category bc
WHERE bp.category_id IS NULL
  AND (
    bp.category = bc.slug
    OR UPPER(REPLACE(bc.slug, '-', '_')) = UPPER(REPLACE(bp.category, '-', '_'))
  );

