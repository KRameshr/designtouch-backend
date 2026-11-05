-- ==========================================================
-- V35__add_is_active_and_is_featured_to_client.sql
-- Add is_active and is_featured columns to client table and backfill existing rows
-- ==========================================================

ALTER TABLE client ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE client ADD COLUMN IF NOT EXISTS is_featured BOOLEAN NOT NULL DEFAULT TRUE;

-- Ensure all existing client records are active and featured by default
UPDATE client
SET is_active = TRUE
WHERE is_active IS NULL;

UPDATE client
SET is_featured = TRUE
WHERE is_featured IS NULL;

