-- ==========================================================
-- V10__create_portfolio_page_content.sql
-- Dedicated singleton table for Portfolio page intro content
-- and table for dynamic category tags
-- ==========================================================

-- 1. Singleton table for Portfolio page intro content
CREATE TABLE portfolio_page_content (
    id                      SMALLINT PRIMARY KEY DEFAULT 1,
    eyebrow                 VARCHAR(150),
    headline_line1          VARCHAR(150),
    headline_line2          VARCHAR(150),
    headline_highlight      VARCHAR(150),
    description             TEXT,
    intro_video_url         TEXT,
    intro_video_public_id   TEXT,
    updated_at              TIMESTAMPTZ DEFAULT now(),
    CONSTRAINT single_row_portfolio_page CHECK (id = 1)
);

-- Seed initial row with approved Portfolio intro copy
INSERT INTO portfolio_page_content (
    id,
    eyebrow,
    headline_line1,
    headline_line2,
    headline_highlight,
    description,
    intro_video_url,
    intro_video_public_id,
    updated_at
) VALUES (
    1,
    'OUR PORTFOLIO // SELECTED WORK',
    'Our Work',
    'Speaks',
    'Louder.',
    'Explore our curated portfolio of commercial campaigns, creator reels, and high-conversion visual narratives built to command attention.',
    NULL,
    NULL,
    now()
);

-- 2. Table for dynamic Portfolio intro category tags
CREATE TABLE portfolio_page_tag (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    label                   VARCHAR(100) NOT NULL,
    display_order           INT DEFAULT 0,
    is_active               BOOLEAN DEFAULT true,
    created_at              TIMESTAMPTZ DEFAULT now(),
    updated_at              TIMESTAMPTZ DEFAULT now()
);

-- Seed initial 5 portfolio tags
INSERT INTO portfolio_page_tag (label, display_order, is_active) VALUES
    ('4K VIDEO EDITING', 1, true),
    ('SHORT-FORM REELS', 2, true),
    ('MOTION GRAPHICS', 3, true),
    ('COMMERCIAL ADS', 4, true),
    ('THUMBNAILS', 5, true);

