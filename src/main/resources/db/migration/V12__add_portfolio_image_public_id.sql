-- ==========================================================
-- V12__add_portfolio_image_public_id.sql
-- Add image_public_id to portfolio_item for Cloudinary image asset tracking
-- ==========================================================

ALTER TABLE portfolio_item ADD COLUMN IF NOT EXISTS image_public_id TEXT;

