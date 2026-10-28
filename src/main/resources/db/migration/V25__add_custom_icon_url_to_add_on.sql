-- ==========================================================
-- V25__add_custom_icon_url_to_add_on.sql
-- Add custom_icon_url and custom_icon_public_id to add_on table
-- ==========================================================

ALTER TABLE add_on ADD COLUMN IF NOT EXISTS custom_icon_url TEXT;
ALTER TABLE add_on ADD COLUMN IF NOT EXISTS custom_icon_public_id TEXT;

