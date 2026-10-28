-- ==========================================================
-- V27__add_blog_post_cover_image_public_id_and_status.sql
-- Add cover_image_public_id, status, and scheduled_for to blog_post
-- ==========================================================

ALTER TABLE blog_post ADD COLUMN IF NOT EXISTS cover_image_public_id TEXT;
ALTER TABLE blog_post ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'PUBLISHED';
ALTER TABLE blog_post ADD COLUMN IF NOT EXISTS scheduled_for TIMESTAMPTZ;

-- Backfill existing rows to ensure consistent status
UPDATE blog_post SET status = CASE
    WHEN is_published = false THEN 'DRAFT'
    ELSE 'PUBLISHED'
END WHERE status IS NULL OR status = '';

CREATE INDEX IF NOT EXISTS idx_blog_post_status ON blog_post(status);
CREATE INDEX IF NOT EXISTS idx_blog_post_scheduled_for ON blog_post(scheduled_for);

