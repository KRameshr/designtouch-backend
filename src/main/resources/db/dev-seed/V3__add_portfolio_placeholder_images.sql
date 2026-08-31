-- ==========================================================
-- V3__add_portfolio_placeholder_images.sql
-- Adds temporary realistic-looking thumbnails to the 3 seeded portfolio
-- items so the Featured Projects grid doesn't look empty during frontend
-- development. Replace with real Cloudinary video/thumbnail URLs once
-- actual footage is uploaded — this is a placeholder-only step.
-- ==========================================================

UPDATE portfolio_item
SET image_url = 'https://picsum.photos/seed/motivation-edit/800/450'
WHERE title = 'Motivation Video Edit';

UPDATE portfolio_item
SET image_url = 'https://picsum.photos/seed/product-ad/800/450'
WHERE title = 'Product Advertisement';

UPDATE portfolio_item
SET image_url = 'https://picsum.photos/seed/luxury-villa/800/450'
WHERE title = 'Luxury Villa Promo';
