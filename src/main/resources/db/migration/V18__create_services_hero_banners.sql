-- ==========================================================
-- V18__create_services_hero_banners.sql
-- Multi-banner storage for Services Hero (max 5 banners)
-- Safely migrates the existing banner from services_page_content
-- ==========================================================

CREATE TABLE IF NOT EXISTS services_hero_banner (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    label               VARCHAR(100),
    image_url           TEXT NOT NULL,
    image_public_id     TEXT,
    display_order       INTEGER NOT NULL DEFAULT 1,
    is_active           BOOLEAN NOT NULL DEFAULT true,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_services_hero_banner_order
    ON services_hero_banner (display_order ASC, created_at ASC);

-- Migrate existing single banner from services_page_content as Banner 1 if present
INSERT INTO services_hero_banner (label, image_url, image_public_id, display_order, is_active, created_at, updated_at)
SELECT
    'Banner 1',
    hero_banner_image_url,
    hero_banner_image_public_id,
    1,
    true,
    now(),
    now()
FROM services_page_content
WHERE id = 1
  AND hero_banner_image_url IS NOT NULL
  AND BTRIM(hero_banner_image_url) <> ''
  AND NOT EXISTS (SELECT 1 FROM services_hero_banner);

