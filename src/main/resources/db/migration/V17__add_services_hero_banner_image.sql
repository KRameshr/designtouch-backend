-- ==========================================================
-- V17__add_services_hero_banner_image.sql
-- Add hero_banner_image_url and hero_banner_image_public_id to services_page_content
-- ==========================================================

ALTER TABLE services_page_content ADD COLUMN IF NOT EXISTS hero_banner_image_url TEXT;
ALTER TABLE services_page_content ADD COLUMN IF NOT EXISTS hero_banner_image_public_id TEXT;

