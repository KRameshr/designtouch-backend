-- ==========================================================
-- V14__add_portfolio_delivery_links_and_slug.sql
-- External video links, client delivery, resource links, and slug
-- ==========================================================

ALTER TABLE portfolio_item
    ADD COLUMN IF NOT EXISTS slug VARCHAR(250),
    ADD COLUMN IF NOT EXISTS live_video_url TEXT,
    ADD COLUMN IF NOT EXISTS google_drive_url TEXT,
    ADD COLUMN IF NOT EXISTS resource_links JSONB DEFAULT '[]'::jsonb;

-- Generate unique slugs for any existing portfolio items
UPDATE portfolio_item
SET slug = LOWER(REGEXP_REPLACE(REGEXP_REPLACE(title, '[^a-zA-Z0-9]+', '-', 'g'), '^-+|-+$', '')) || '-' || SUBSTRING(id::text, 1, 8)
WHERE slug IS NULL OR slug = '';

-- Enforce NOT NULL on slug after backfilling
ALTER TABLE portfolio_item
    ALTER COLUMN slug SET NOT NULL;

-- Unique constraint index on slug
CREATE UNIQUE INDEX IF NOT EXISTS uq_portfolio_item_slug ON portfolio_item(slug);

