-- ==========================================================
-- V6__add_thumbnail_design_items.sql
-- Adds 10 THUMBNAIL_DESIGN portfolio items matching the Figma "THUMBNAIL
-- DESIGN" grid. category is left NULL (thumbnails use subCategory instead,
-- per the PortfolioItem design — see the contentType/category discussion
-- in the earlier database design plan).
-- ==========================================================

INSERT INTO portfolio_item
    (title, content_type, sub_category, image_url, is_featured, display_order)
VALUES
    ('5 Habits That Changed My Life', 'THUMBNAIL_DESIGN', 'Motivation Content',
     'https://picsum.photos/seed/thumb-habits/400/225', false, 1),
    ('How I Made ₹10 Lakh Online', 'THUMBNAIL_DESIGN', 'Finance Content',
     'https://picsum.photos/seed/thumb-money/400/225', false, 2),
    ('Top 10 AI Tools in 2024', 'THUMBNAIL_DESIGN', 'Tech Content',
     'https://picsum.photos/seed/thumb-ai/400/225', false, 3),
    ('Complete Video Editing Guide', 'THUMBNAIL_DESIGN', 'Tutorial Content',
     'https://picsum.photos/seed/thumb-editing/400/225', false, 4),
    ('College vs Real Life', 'THUMBNAIL_DESIGN', 'Entertainment Content',
     'https://picsum.photos/seed/thumb-college/400/225', false, 5),
    ('My Productive Morning Routine', 'THUMBNAIL_DESIGN', 'Lifestyle Content',
     'https://picsum.photos/seed/thumb-morning/400/225', false, 6),
    ('Best Laptops Under ₹50,000', 'THUMBNAIL_DESIGN', 'Tech Review',
     'https://picsum.photos/seed/thumb-laptops/400/225', false, 7),
    ('Day in My Life', 'THUMBNAIL_DESIGN', 'Vlog Content',
     'https://picsum.photos/seed/thumb-dayinmylife/400/225', false, 8),
    ('Don''t Make These Mistakes!', 'THUMBNAIL_DESIGN', 'Education Content',
     'https://picsum.photos/seed/thumb-mistakes/400/225', false, 9),
    ('60 FPS vs 30 FPS', 'THUMBNAIL_DESIGN', 'Gaming Content',
     'https://picsum.photos/seed/thumb-fps/400/225', false, 10);
