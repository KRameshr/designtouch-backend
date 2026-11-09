-- ==========================================================
-- V5__add_remaining_services.sql
-- V2 only seeded 2 of the 6 services shown in the Figma "OUR SERVICES"
-- grid. Adds the remaining 4, matching icon/color/order from the design.
-- ==========================================================

INSERT INTO service (icon, title, description, features, color_theme, slug, order_number, display_order) VALUES
    ('social', 'Social Media Content', 'Scroll-stopping content designed to grow your brand and boost engagement.',
     ARRAY['Instagram Reels', 'YouTube Shorts', 'TikTok Videos', 'Ad Creatives'],
     'orange', 'social-media-content', '03', 3),
    ('youtube', 'YouTube Management', 'End-to-end YouTube solutions to grow your channel and maximize views.',
     ARRAY['Video Editing', 'Thumbnails', 'SEO & Optimization', 'Channel Growth'],
     'purple', 'youtube-management', '04', 4),
    ('graphic', 'Graphic Design', 'Creative designs that communicate your brand identity with impact.',
     ARRAY['Social Media Posts', 'Banners & Posters', 'Brand Identity', 'Marketing Materials'],
     'blue', 'graphic-design', '05', 5),
    ('branding', 'Branding', 'Build a strong brand identity that makes you stand out from the competition.',
     ARRAY['Logo Design', 'Brand Guidelines', 'Brand Strategy', 'Visual Identity'],
     'yellow', 'branding', '06', 6);

-- Also backfill color_theme on the 2 services V2 already inserted, since
-- that migration predates the colorTheme usage in the frontend.
UPDATE service SET color_theme = 'red' WHERE slug = 'video-editing';
UPDATE service SET color_theme = 'green' WHERE slug = 'motion-graphics';
