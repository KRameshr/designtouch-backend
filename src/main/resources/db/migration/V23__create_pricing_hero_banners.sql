-- ==========================================================
-- V23__create_pricing_hero_banners.sql
-- Multi-banner storage for Pricing Hero (max 5 banners)
-- Safely migrates the existing banner from pricing_page_content
-- ==========================================================

CREATE TABLE IF NOT EXISTS pricing_hero_banner (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    label               VARCHAR(100),
    image_url           TEXT NOT NULL,
    image_public_id     TEXT,
    alt_text            VARCHAR(255) DEFAULT 'DesignTouch+ Creative Production Studio',
    object_position     VARCHAR(50) DEFAULT 'center',
    display_order       INTEGER NOT NULL DEFAULT 1,
    is_active           BOOLEAN NOT NULL DEFAULT true,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_pricing_hero_banner_order
    ON pricing_hero_banner (display_order ASC, created_at ASC);

-- Migrate existing single banner from pricing_page_content as Banner 1 if present
INSERT INTO pricing_hero_banner (label, image_url, image_public_id, alt_text, object_position, display_order, is_active, created_at, updated_at)
SELECT
    'Banner 1',
    COALESCE(hero_banner_image_url, hero_background_image_url),
    hero_banner_image_public_id,
    COALESCE(hero_banner_image_alt, 'DesignTouch+ Creative Production Studio'),
    COALESCE(hero_banner_object_position, 'center'),
    1,
    true,
    now(),
    now()
FROM pricing_page_content
WHERE id = 1
  AND (
    (hero_banner_image_url IS NOT NULL AND BTRIM(hero_banner_image_url) <> '')
    OR (hero_background_image_url IS NOT NULL AND BTRIM(hero_background_image_url) <> '')
  )
  AND NOT EXISTS (SELECT 1 FROM pricing_hero_banner);

