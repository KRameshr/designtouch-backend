-- ==========================================================
-- V9__add_portfolio_video_public_id.sql
-- Add video_public_id to portfolio_item & enhance stat_item
-- ==========================================================

-- 1. Add video_public_id to portfolio_item
ALTER TABLE portfolio_item ADD COLUMN IF NOT EXISTS video_public_id TEXT;

-- 2. Enhance stat_item to support string values (e.g. '80+', '10+')
ALTER TABLE stat_item ALTER COLUMN value TYPE VARCHAR(50) USING value::VARCHAR;
ALTER TABLE stat_item ALTER COLUMN label TYPE VARCHAR(150);
ALTER TABLE stat_item ALTER COLUMN description DROP NOT NULL;

-- 3. Set default portfolio stats to the requested metrics:
-- Projects Completed (80+), Happy Clients (4+), Years Experience (10+), Countries Served (10+)
UPDATE stat_item SET label = 'Projects Completed', value = '80+', suffix = '+', description = 'Projects Completed', display_order = 1 WHERE display_order = 1;
UPDATE stat_item SET label = 'Happy Clients', value = '4+', suffix = '+', description = 'Happy Clients', display_order = 2 WHERE display_order = 2;
UPDATE stat_item SET label = 'Years Experience', value = '10+', suffix = '+', description = 'Years Experience', display_order = 3 WHERE display_order = 3;
UPDATE stat_item SET label = 'Countries Served', value = '10+', suffix = '+', description = 'Countries Served', display_order = 4 WHERE display_order = 4;
