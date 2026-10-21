-- ==========================================================
-- V16__create_services_page_content.sql
-- Singleton table for public Services page CMS content,
-- enhancements to service, process_step, and tool tables
-- ==========================================================

-- 1. Singleton table for Services Page CMS content
CREATE TABLE IF NOT EXISTS services_page_content (
    id                          SMALLINT PRIMARY KEY DEFAULT 1,

    -- Hero Section
    hero_eyebrow                VARCHAR(150),
    hero_headline_line1         VARCHAR(150),
    hero_headline_line2         VARCHAR(150),
    hero_headline_highlight     VARCHAR(150),
    hero_description            TEXT,
    hero_primary_cta_text       VARCHAR(100),
    hero_primary_cta_link       VARCHAR(200),
    hero_secondary_cta_text     VARCHAR(100),
    hero_secondary_cta_link     VARCHAR(200),
    hero_video_url              TEXT,
    hero_stat1_value            VARCHAR(50),
    hero_stat1_label            VARCHAR(100),
    hero_stat2_value            VARCHAR(50),
    hero_stat2_label            VARCHAR(100),
    hero_stat3_value            VARCHAR(50),
    hero_stat3_label            VARCHAR(100),
    hero_show_stats             BOOLEAN NOT NULL DEFAULT true,
    hero_show_workstation       BOOLEAN NOT NULL DEFAULT true,

    -- Section Headers
    services_eyebrow            VARCHAR(150),
    services_heading            VARCHAR(150),
    services_description        TEXT,

    process_eyebrow             VARCHAR(150),
    process_heading             VARCHAR(150),
    process_description         TEXT,

    tools_eyebrow               VARCHAR(150),
    tools_heading               VARCHAR(150),
    tools_description           TEXT,

    -- Bottom CTA Section
    cta_eyebrow                 VARCHAR(150),
    cta_heading_line1           VARCHAR(150),
    cta_heading_line2           VARCHAR(150),
    cta_heading_highlight       VARCHAR(150),
    cta_description             TEXT,
    cta_button_text             VARCHAR(100),
    cta_button_link             VARCHAR(200),
    cta_accent_note             VARCHAR(150),

    updated_at                  TIMESTAMPTZ DEFAULT now(),
    CONSTRAINT single_row_services_page CHECK (id = 1)
);

-- Seed initial row with approved Services page content
INSERT INTO services_page_content (
    id,
    hero_eyebrow,
    hero_headline_line1,
    hero_headline_line2,
    hero_headline_highlight,
    hero_description,
    hero_primary_cta_text,
    hero_primary_cta_link,
    hero_secondary_cta_text,
    hero_secondary_cta_link,
    hero_video_url,
    hero_stat1_value,
    hero_stat1_label,
    hero_stat2_value,
    hero_stat2_label,
    hero_stat3_value,
    hero_stat3_label,
    hero_show_stats,
    hero_show_workstation,
    services_eyebrow,
    services_heading,
    services_description,
    process_eyebrow,
    process_heading,
    process_description,
    tools_eyebrow,
    tools_heading,
    tools_description,
    cta_eyebrow,
    cta_heading_line1,
    cta_heading_line2,
    cta_heading_highlight,
    cta_description,
    cta_button_text,
    cta_button_link,
    cta_accent_note,
    updated_at
) VALUES (
    1,
    'OUR SERVICES // FULL-SERVICE CREATIVE STUDIO',
    'CREATIVE SOLUTIONS',
    'THAT DRIVE',
    'RESULTS.',
    'From concept to final cut, we provide end-to-end creative services that help brands connect, engage, and grow. Every edit, thumbnail, and motion visual is crafted for maximum retention and measurable conversion.',
    'DISCUSS YOUR PROJECT',
    '/contact',
    'VIEW WORK ARCHIVE',
    '/portfolio',
    '/hero-demo.mp4',
    '4K+',
    'Master Exports',
    '48H',
    'Avg First Draft',
    '100%',
    'Retention Focus',
    true,
    true,
    'WHAT WE DO // PORTFOLIO DISCIPLINES',
    'OUR SERVICES',
    'High-quality creative services tailored to your brand’s unique needs. Engineered for digital retention and commercial performance.',
    'HOW WE WORK // METHODOLOGY',
    'OUR PROCESS',
    'A structured five-step production workflow built for creative alignment, swift turnaround, and measurable results.',
    'PRODUCTION STACK // HARDWARE & SOFTWARE',
    'TOOLS WE USE',
    'Industry-standard software suites power every frame, cut, grade, and render we produce for our clients.',
    'STUDIO AVAILABILITY // NOW ACCEPTING COMMISSIONS',
    'LET’S CREATE SOMETHING',
    'AMAZING',
    'TOGETHER.',
    'Have a project in mind? We''re ready to bring your ideas to life with high-retention video editing, scroll-stopping thumbnails, and cinematic visual assets.',
    'GET IN TOUCH',
    '/contact',
    '● STUDIO AVAILABILITY // NOW ACCEPTING 2026 PROJECTS',
    now()
) ON CONFLICT (id) DO NOTHING;

-- 2. Extend the service table
ALTER TABLE service
    ADD COLUMN IF NOT EXISTS short_description TEXT,
    ADD COLUMN IF NOT EXISTS full_description  TEXT,
    ADD COLUMN IF NOT EXISTS cta_label         VARCHAR(100),
    ADD COLUMN IF NOT EXISTS cta_url           VARCHAR(255),
    ADD COLUMN IF NOT EXISTS image_url         TEXT,
    ADD COLUMN IF NOT EXISTS is_active         BOOLEAN NOT NULL DEFAULT true;

-- Backfill short_description for existing records
UPDATE service SET short_description = description WHERE short_description IS NULL;

-- Ensure all 8 canonical services exist in the database
INSERT INTO service (icon, title, description, short_description, features, color_theme, slug, order_number, display_order, is_active)
VALUES
    ('video', 'Video Editing', 'Engaging edits for YouTube, social media, ads, podcasts, and digital campaigns.',
     'Engaging edits for YouTube, social media, ads, podcasts, and digital campaigns.',
     ARRAY['Long-form & Short-form Cuts', 'Dynamic Pacing & Retention Flow', 'Podcast & Interview Editing', 'Cinematic Color Grading'],
     'red', 'video-editing', '01', 1, true),
    ('motion', 'Motion Graphics', 'Stunning motion graphics that explain, engage, and elevate your core narrative.',
     'Stunning motion graphics that explain, engage, and elevate your core narrative.',
     ARRAY['2D & 3D Title Animation', 'Explainer Video Sequences', 'Logo Idents & Sound Stings', 'Kinetic Typography Packs'],
     'green', 'motion-graphics', '02', 2, true),
    ('social', 'Social Media Content', 'Scroll-stopping vertical video designed to capture attention and scale reach.',
     'Scroll-stopping vertical video designed to capture attention and scale reach.',
     ARRAY['Instagram Reels & TikToks', 'YouTube Shorts Optimization', 'Viral Hook Formatting', 'Animated Captions & B-Roll'],
     'orange', 'social-media-content', '03', 3, true),
    ('youtube', 'YouTube Management', 'End-to-end channel operations to optimize algorithm performance and view counts.',
     'End-to-end channel operations to optimize algorithm performance and view counts.',
     ARRAY['Upload Schedule & Publishing', 'High-CTR Title Strategies', 'Audience Retention Audits', 'Community & Tag SEO'],
     'purple', 'youtube-management', '04', 4, true),
    ('graphic', 'Graphic Design', 'High-impact visual designs that communicate your brand message with authority.',
     'High-impact visual designs that communicate your brand message with authority.',
     ARRAY['Social Media Carousels', 'Marketing Banners & Post Art', 'Pitch Decks & Presentation Kits', 'Advertising Creative Suites'],
     'blue', 'graphic-design', '05', 5, true),
    ('thumbnail', 'Thumbnail Design', 'High-CTR, psychology-driven custom thumbnails engineered for peak click rates.',
     'High-CTR, psychology-driven custom thumbnails engineered for peak click rates.',
     ARRAY['High-CTR Visual Formulas', '3D Typography & Bold Callouts', 'Face Retouching & Expression Pop', 'A/B Split Test Ready Files'],
     'red', 'thumbnail-design', '06', 6, true),
    ('branding', 'Branding', 'Cohesive brand identities that distinguish your studio from competitors.',
     'Cohesive brand identities that distinguish your studio from competitors.',
     ARRAY['Logo System & Variations', 'Color Palettes & Typography', 'Brand Guidelines Manual', 'Social Media Kit Assets'],
     'yellow', 'branding', '07', 7, true),
    ('commercial', 'Commercial & Ads', 'Commercial-grade video assets structured specifically for paid conversion.',
     'Commercial-grade video assets structured specifically for paid conversion.',
     ARRAY['Direct Response Ad Cuts', 'Multi-Ratio Ad Adaptations', 'Product Demo & Explainer Ads', 'Sound Design & Voice Mixing'],
     'green', 'commercial-ads', '08', 8, true)
ON CONFLICT (slug) DO UPDATE SET
    icon = EXCLUDED.icon,
    color_theme = EXCLUDED.color_theme,
    order_number = EXCLUDED.order_number;

-- 3. Extend the process_step table with is_active
ALTER TABLE process_step
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT true;

-- Seed the 5 standard steps for SERVICES page
INSERT INTO process_step (page_type, step_number, title, description, icon, display_order, is_active)
VALUES
    ('SERVICES', 1, 'Discover', 'We analyze your brand voice, content goals, audience metrics, and raw source footage to establish a clear creative brief.', 'discover', 1, true),
    ('SERVICES', 2, 'Plan', 'We outline narrative arcs, pacing blueprints, sound design references, and milestone schedules before touching the edit.', 'plan', 2, true),
    ('SERVICES', 3, 'Create', 'Our dedicated editors and motion designers craft the assembly, rhythm, B-roll layering, custom graphics, and color grading.', 'create', 3, true),
    ('SERVICES', 4, 'Review', 'Structured feedback cycles let you review timecoded cuts and request precision adjustments until the final edit is dialed in.', 'review', 4, true),
    ('SERVICES', 5, 'Deliver', 'Master exports are prepared in 4K ProRes, YouTube landscape, and 9:16 vertical cuts, securely delivered and archived.', 'deliver', 5, true)
ON CONFLICT DO NOTHING;

-- 4. Extend the tool table
ALTER TABLE tool
    ADD COLUMN IF NOT EXISTS logo_url     TEXT,
    ADD COLUMN IF NOT EXISTS website_url VARCHAR(255),
    ADD COLUMN IF NOT EXISTS role        VARCHAR(100),
    ADD COLUMN IF NOT EXISTS is_active   BOOLEAN NOT NULL DEFAULT true;

-- Seed canonical 7 tools
INSERT INTO tool (name, icon_key, role, display_order, is_active)
VALUES
    ('Adobe Premiere Pro', 'Pr', 'NLE & Timeline Assembly', 1, true),
    ('After Effects', 'Ae', 'Motion & Visual Effects', 2, true),
    ('DaVinci Resolve', 'Dv', 'Color Grading & Mastering', 3, true),
    ('Photoshop', 'Ps', 'Thumbnail & Raster Art', 4, true),
    ('Illustrator', 'Ai', 'Vector Graphics & Branding', 5, true),
    ('Blender', '3D', '3D Animation & Assets', 6, true),
    ('Figma', 'Fg', 'Storyboards & UI Layout', 7, true)
ON CONFLICT DO NOTHING;

