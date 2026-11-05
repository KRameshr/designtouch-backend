-- ==========================================================
-- V37__add_about_hero_banner_and_story_fields.sql
-- Add hero banner, CTA, and story fields to about_content
-- ==========================================================

ALTER TABLE about_content
    ADD COLUMN IF NOT EXISTS cta_text VARCHAR(100) DEFAULT 'Let''s Create Together',
    ADD COLUMN IF NOT EXISTS cta_link VARCHAR(200) DEFAULT '/contact',
    ADD COLUMN IF NOT EXISTS hero_banner_image_url TEXT,
    ADD COLUMN IF NOT EXISTS hero_banner_image_public_id TEXT,
    ADD COLUMN IF NOT EXISTS story_heading VARCHAR(200) DEFAULT 'Turning Ideas Into Impact.',
    ADD COLUMN IF NOT EXISTS story_image_url TEXT;

UPDATE about_content
SET cta_text = COALESCE(cta_text, 'Let''s Create Together'),
    cta_link = COALESCE(cta_link, '/contact'),
    story_heading = COALESCE(story_heading, 'Turning Ideas Into Impact.'),
    story_image_url = COALESCE(story_image_url, story_video_thumbnail_url)
WHERE id = 1;

