-- ==========================================================
-- V8__create_hero_content.sql
-- Singleton table for homepage Hero section content
-- ==========================================================

CREATE TABLE hero_content (
    id                      SMALLINT PRIMARY KEY DEFAULT 1,
    eyebrow                 VARCHAR(150),
    headline_line1          VARCHAR(150),
    headline_line2          VARCHAR(150),
    headline_line3          VARCHAR(150),
    headline_highlight      VARCHAR(150),
    subheading              TEXT,
    primary_cta_text        VARCHAR(100),
    primary_cta_link        VARCHAR(200),
    secondary_cta_text      VARCHAR(100),
    secondary_cta_link      VARCHAR(200),
    video_url               TEXT,
    badge_text              VARCHAR(100),
    spec_badge_text         VARCHAR(100),
    updated_at              TIMESTAMPTZ DEFAULT now(),
    CONSTRAINT single_row_hero CHECK (id = 1)
);

-- Seed initial row using currently approved Hero content
INSERT INTO hero_content (
    id,
    eyebrow,
    headline_line1,
    headline_line2,
    headline_line3,
    headline_highlight,
    subheading,
    primary_cta_text,
    primary_cta_link,
    secondary_cta_text,
    secondary_cta_link,
    video_url,
    badge_text,
    spec_badge_text,
    updated_at
) VALUES (
    1,
    'Welcome to DesignTouch+',
    'We create',
    'videos that',
    'grow',
    'brands.',
    'We help businesses and creators tell their stories through powerful video editing & creative content.',
    'View Portfolio',
    '/portfolio',
    'Book a Call',
    '/contact',
    '/hero-demo.mp4',
    'REEL // 2026',
    '4K PRORES',
    now()
);

