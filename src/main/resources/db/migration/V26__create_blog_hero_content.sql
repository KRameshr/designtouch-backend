-- ==========================================================
-- V26__create_blog_hero_content.sql
-- Singleton table for public Blog page Hero CMS content
-- ==========================================================

CREATE TABLE IF NOT EXISTS blog_page_content (
    id                          SMALLINT PRIMARY KEY DEFAULT 1,
    hero_eyebrow                VARCHAR(150) DEFAULT 'OUR BLOG',
    hero_headline_line1         VARCHAR(150) DEFAULT 'INSIGHTS THAT',
    hero_headline_line2         VARCHAR(150) DEFAULT 'INSPIRE',
    hero_headline_highlight     VARCHAR(150) DEFAULT 'CREATIVITY.',
    hero_description            TEXT DEFAULT 'Tips, trends and tutorials on video editing, motion graphics, content creation and growing your brand.',
    hero_cta_text               VARCHAR(100) DEFAULT 'EXPLORE ALL ARTICLES',
    hero_cta_link               VARCHAR(200) DEFAULT '#articles',
    hero_banner_image_url       TEXT,
    hero_banner_image_public_id TEXT,
    hero_banner_image_alt       VARCHAR(255) DEFAULT 'DesignTouch+ Creative Workspace Studio',
    hero_banner_object_position VARCHAR(50) DEFAULT 'center right',
    updated_at                  TIMESTAMPTZ DEFAULT now(),
    CONSTRAINT single_row_blog_page CHECK (id = 1)
);

-- Seed initial row with approved default Blog Hero content and creative workspace banner
INSERT INTO blog_page_content (
    id,
    hero_eyebrow,
    hero_headline_line1,
    hero_headline_line2,
    hero_headline_highlight,
    hero_description,
    hero_cta_text,
    hero_cta_link,
    hero_banner_image_url,
    hero_banner_image_public_id,
    hero_banner_image_alt,
    hero_banner_object_position,
    updated_at
) VALUES (
    1,
    'OUR BLOG',
    'INSIGHTS THAT',
    'INSPIRE',
    'CREATIVITY.',
    'Tips, trends and tutorials on video editing, motion graphics, content creation and growing your brand.',
    'EXPLORE ALL ARTICLES',
    '#articles',
    'https://res.cloudinary.com/dggikfrj5/image/upload/v1792669852/designtouch/services/cygu80p1hmhkqgvpc3au.png',
    'designtouch/services/cygu80p1hmhkqgvpc3au',
    'DesignTouch+ Creative Workspace Studio',
    'center right',
    now()
) ON CONFLICT (id) DO NOTHING;
