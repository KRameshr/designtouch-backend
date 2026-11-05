-- ==========================================================
-- V38__add_founder_designation_to_about_content.sql
-- Add founder_designation column to about_content table
-- ==========================================================

ALTER TABLE about_content
    ADD COLUMN IF NOT EXISTS founder_designation VARCHAR(150) DEFAULT 'Founder, DesignTouch+';

UPDATE about_content
SET founder_designation = 'Founder, DesignTouch+'
WHERE id = 1 AND founder_designation IS NULL;

