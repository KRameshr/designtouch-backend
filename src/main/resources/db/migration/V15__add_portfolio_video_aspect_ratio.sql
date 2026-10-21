-- Migration V15: Add video_aspect_ratio column to portfolio_item
ALTER TABLE portfolio_item
ADD COLUMN IF NOT EXISTS video_aspect_ratio VARCHAR(20) DEFAULT 'AUTO';

-- Backfill existing video editing projects to 'AUTO'
UPDATE portfolio_item
SET video_aspect_ratio = 'AUTO'
WHERE content_type = 'VIDEO_EDITING' AND (video_aspect_ratio IS NULL OR video_aspect_ratio = '');

