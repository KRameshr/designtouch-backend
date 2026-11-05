-- ==========================================================
-- V34__add_service_type_to_testimonial.sql
-- Add service_type column to testimonial table and backfill existing rows
-- ==========================================================

ALTER TABLE testimonial ADD COLUMN IF NOT EXISTS service_type VARCHAR(100);

-- Backfill existing rows without a service type to 'General Feedback'
UPDATE testimonial
SET service_type = 'General Feedback'
WHERE service_type IS NULL OR trim(service_type) = '';

