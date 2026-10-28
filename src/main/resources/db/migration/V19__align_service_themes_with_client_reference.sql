-- ==========================================================
-- V19__align_service_themes_with_client_reference.sql
-- Align the 8 canonical service accent color themes with the
-- client reference palette (red, green, violet, orange, cyan,
-- yellow, pink, green) while preserving all existing content.
-- ==========================================================

UPDATE service SET color_theme = 'red',    display_order = 1 WHERE slug = 'video-editing';
UPDATE service SET color_theme = 'green',  display_order = 2 WHERE slug = 'motion-graphics';
UPDATE service SET color_theme = 'violet', display_order = 3 WHERE slug = 'social-media-content';
UPDATE service SET color_theme = 'orange', display_order = 4 WHERE slug = 'youtube-management';
UPDATE service SET color_theme = 'cyan',   display_order = 5 WHERE slug = 'graphic-design';
UPDATE service SET color_theme = 'yellow', display_order = 6 WHERE slug = 'thumbnail-design';
UPDATE service SET color_theme = 'pink',   display_order = 7 WHERE slug = 'branding';
UPDATE service SET color_theme = 'green',  display_order = 8 WHERE slug = 'commercial-ads';

