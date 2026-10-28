-- ==========================================================
-- V22__add_pricing_hero_banner_and_cta_fields.sql
-- Add banner image, alt, focal position, highlight style, and CTA fields
-- to pricing_page_content table.
-- ==========================================================

ALTER TABLE pricing_page_content ADD COLUMN IF NOT EXISTS hero_banner_image_url TEXT;
ALTER TABLE pricing_page_content ADD COLUMN IF NOT EXISTS hero_banner_image_public_id TEXT;
ALTER TABLE pricing_page_content ADD COLUMN IF NOT EXISTS hero_banner_image_alt VARCHAR(255) DEFAULT 'DesignTouch+ Creative Production Studio';
ALTER TABLE pricing_page_content ADD COLUMN IF NOT EXISTS hero_banner_object_position VARCHAR(50) DEFAULT 'center';
ALTER TABLE pricing_page_content ADD COLUMN IF NOT EXISTS hero_highlight_style VARCHAR(50) DEFAULT 'gradient';
ALTER TABLE pricing_page_content ADD COLUMN IF NOT EXISTS hero_primary_cta_text VARCHAR(100) DEFAULT 'CHOOSE A PLAN';
ALTER TABLE pricing_page_content ADD COLUMN IF NOT EXISTS hero_primary_cta_link VARCHAR(200) DEFAULT '#plans';
ALTER TABLE pricing_page_content ADD COLUMN IF NOT EXISTS hero_secondary_cta_text VARCHAR(100) DEFAULT 'BOOK A CALL';
ALTER TABLE pricing_page_content ADD COLUMN IF NOT EXISTS hero_secondary_cta_link VARCHAR(200) DEFAULT '/contact';

-- Sync legacy hero_background_image_url if present
UPDATE pricing_page_content
SET hero_banner_image_url = hero_background_image_url
WHERE hero_banner_image_url IS NULL AND hero_background_image_url IS NOT NULL;

-- Initialize default values for existing row 1 if null
UPDATE pricing_page_content
SET hero_banner_image_alt = COALESCE(hero_banner_image_alt, 'DesignTouch+ Creative Production Studio'),
    hero_banner_object_position = COALESCE(hero_banner_object_position, 'center'),
    hero_highlight_style = COALESCE(hero_highlight_style, 'gradient'),
    hero_primary_cta_text = COALESCE(hero_primary_cta_text, 'CHOOSE A PLAN'),
    hero_primary_cta_link = COALESCE(hero_primary_cta_link, '#plans'),
    hero_secondary_cta_text = COALESCE(hero_secondary_cta_text, 'BOOK A CALL'),
    hero_secondary_cta_link = COALESCE(hero_secondary_cta_link, '/contact')
WHERE id = 1;

