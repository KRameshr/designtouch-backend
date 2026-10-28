-- ==========================================================
-- V29__add_blog_post_focus_keywords.sql
-- Add focus_keywords text array to blog_post for editorial target search phrases
-- ==========================================================

ALTER TABLE blog_post ADD COLUMN IF NOT EXISTS focus_keywords TEXT[] DEFAULT '{}';

