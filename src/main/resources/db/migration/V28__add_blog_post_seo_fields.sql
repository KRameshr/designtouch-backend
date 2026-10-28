-- ==========================================================
-- V28__add_blog_post_seo_fields.sql
-- Add seo_title, meta_description, and tags to blog_post
-- ==========================================================

ALTER TABLE blog_post ADD COLUMN IF NOT EXISTS seo_title VARCHAR(300);
ALTER TABLE blog_post ADD COLUMN IF NOT EXISTS meta_description TEXT;
ALTER TABLE blog_post ADD COLUMN IF NOT EXISTS tags TEXT[] DEFAULT '{}';

