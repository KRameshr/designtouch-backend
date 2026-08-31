-- ==========================================================
-- V4__add_fourth_featured_portfolio_item.sql
-- Adds the 4th featured portfolio item ("Fitness Reel Edit") to match
-- the Figma design's 4-column Featured Projects grid. V2 only seeded 3.
-- Placeholder image included directly (new row, no separate UPDATE needed).
-- ==========================================================

INSERT INTO portfolio_item
    (title, content_type, category, description, duration, video_url, image_url, is_featured, display_order)
VALUES
    ('Fitness Reel Edit', 'VIDEO_EDITING', 'INSTAGRAM_REELS',
     'Fast-paced reel edit with trendy transitions.', '00:30', NULL,
     'https://picsum.photos/seed/fitness-reel/800/450', true, 4);
