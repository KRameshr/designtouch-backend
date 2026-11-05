-- ==========================================================
-- V32__create_contact_page_content.sql
-- Singleton table for public Contact page Hero CMS content
-- ==========================================================

CREATE TABLE IF NOT EXISTS contact_page_content (
    id                          SMALLINT PRIMARY KEY DEFAULT 1,
    hero_eyebrow                VARCHAR(150) DEFAULT 'Contact Us',
    hero_headline_line1         VARCHAR(150) DEFAULT 'Let''s Create Something',
    hero_headline_highlight     VARCHAR(150) DEFAULT 'Amazing.',
    hero_description            TEXT DEFAULT 'Have a project in mind? We''d love to hear about it. Fill out the form or reach out to us directly.',
    badge1_title                VARCHAR(100) DEFAULT 'Fast Response',
    badge1_subtitle             VARCHAR(150) DEFAULT 'We reply within 24 hours',
    badge2_title                VARCHAR(100) DEFAULT 'Project Focused',
    badge2_subtitle             VARCHAR(150) DEFAULT 'Solutions tailored for you',
    hero_banner_image_url       TEXT,
    hero_banner_image_public_id TEXT,
    hero_banner_image_alt       VARCHAR(255) DEFAULT 'DesignTouch+ Creative Workstation',
    hero_banner_object_position VARCHAR(50) DEFAULT 'center',
    updated_at                  TIMESTAMPTZ DEFAULT now(),
    CONSTRAINT single_row_contact CHECK (id = 1)
);

-- Seed initial row with approved default Contact Hero content
INSERT INTO contact_page_content (
    id,
    hero_eyebrow,
    hero_headline_line1,
    hero_headline_highlight,
    hero_description,
    badge1_title,
    badge1_subtitle,
    badge2_title,
    badge2_subtitle,
    hero_banner_image_url,
    hero_banner_image_public_id,
    hero_banner_image_alt,
    hero_banner_object_position,
    updated_at
) VALUES (
    1,
    'Contact Us',
    'Let''s Create Something',
    'Amazing.',
    'Have a project in mind? We''d love to hear about it. Fill out the form or reach out to us directly.',
    'Fast Response',
    'We reply within 24 hours',
    'Project Focused',
    'Solutions tailored for you',
    NULL,
    NULL,
    'DesignTouch+ Creative Workstation',
    'center',
    now()
) ON CONFLICT (id) DO NOTHING;

