-- V21: Create pricing_page_content and align Pricing CMS tables

CREATE TABLE IF NOT EXISTS pricing_page_content (
    id SMALLINT PRIMARY KEY DEFAULT 1,
    hero_eyebrow VARCHAR(150) DEFAULT 'PRICING PLANS',
    hero_headline_line1 VARCHAR(150) DEFAULT 'SIMPLE PRICING.',
    hero_headline_line2 VARCHAR(150) DEFAULT 'POWERFUL',
    hero_headline_highlight VARCHAR(150) DEFAULT 'RESULTS.',
    hero_description TEXT DEFAULT 'Choose the perfect plan for your needs. All plans include high-quality editing, fast delivery and unlimited revisions.',
    hero_background_image_url TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO pricing_page_content (
    id,
    hero_eyebrow,
    hero_headline_line1,
    hero_headline_line2,
    hero_headline_highlight,
    hero_description,
    hero_background_image_url
) VALUES (
    1,
    'PRICING PLANS',
    'SIMPLE PRICING.',
    'POWERFUL',
    'RESULTS.',
    'Choose the perfect plan for your needs. All plans include high-quality editing, fast delivery and unlimited revisions.',
    NULL
)
ON CONFLICT (id) DO NOTHING;

-- Add is_active and badge/accent customization to pricing_plan
ALTER TABLE pricing_plan ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE pricing_plan ADD COLUMN IF NOT EXISTS badge_text VARCHAR(50);
ALTER TABLE pricing_plan ADD COLUMN IF NOT EXISTS accent_color VARCHAR(50);

-- Backfill badge_text for the most popular plans
UPDATE pricing_plan
SET badge_text = 'MOST POPULAR'
WHERE is_most_popular = TRUE AND (badge_text IS NULL OR badge_text = '');

-- Add is_active to add_on
ALTER TABLE add_on ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;

-- Add is_active to faq
ALTER TABLE faq ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;

-- Update or seed the 8 PRICING FAQs from reference design
UPDATE faq
SET question = 'How many revisions do I get?',
    answer = 'Each plan includes revisions to fine-tune your cuts. Basic includes 2 revisions, while Standard, Premium, and Enterprise plans offer unlimited revisions.',
    display_order = 2,
    is_active = true
WHERE category = 'PRICING' AND question LIKE 'Do you offer revisions%';

INSERT INTO faq (question, answer, category, display_order, is_active)
SELECT 'What if I need the video urgently?', 'We offer rush delivery options as an add-on to fast-track your project within 24 to 48 hours depending on scope.', 'PRICING'::faq_category, 3, true
WHERE NOT EXISTS (SELECT 1 FROM faq WHERE category = 'PRICING' AND question = 'What if I need the video urgently?');

INSERT INTO faq (question, answer, category, display_order, is_active)
SELECT 'What software do you use?', 'We use industry-standard creative software including Adobe Premiere Pro, After Effects, DaVinci Resolve, Photoshop, Illustrator, and Blender.', 'PRICING'::faq_category, 4, true
WHERE NOT EXISTS (SELECT 1 FROM faq WHERE category = 'PRICING' AND question = 'What software do you use?');

INSERT INTO faq (question, answer, category, display_order, is_active)
SELECT 'Can you work on long-term projects?', 'Yes! Our monthly packages and enterprise plans are specifically designed for ongoing, high-volume collaborations with dedicated editors.', 'PRICING'::faq_category, 5, true
WHERE NOT EXISTS (SELECT 1 FROM faq WHERE category = 'PRICING' AND question = 'Can you work on long-term projects?');

INSERT INTO faq (question, answer, category, display_order, is_active)
SELECT 'Do you provide voice overs?', 'Voice-over recording and AI voice generation can be included as an add-on or tailored directly into your custom enterprise package.', 'PRICING'::faq_category, 6, true
WHERE NOT EXISTS (SELECT 1 FROM faq WHERE category = 'PRICING' AND question = 'Do you provide voice overs?');

INSERT INTO faq (question, answer, category, display_order, is_active)
SELECT 'Do you offer refunds?', 'We work closely with you through draft reviews and revisions to guarantee satisfaction. If we haven''t started production, full refunds are available upon request.', 'PRICING'::faq_category, 7, true
WHERE NOT EXISTS (SELECT 1 FROM faq WHERE category = 'PRICING' AND question = 'Do you offer refunds?');

INSERT INTO faq (question, answer, category, display_order, is_active)
SELECT 'Can I upgrade my plan later?', 'Absolutely. You can easily upgrade from a single-project tier to a monthly retainer or scale up your package anytime through our team.', 'PRICING'::faq_category, 8, true
WHERE NOT EXISTS (SELECT 1 FROM faq WHERE category = 'PRICING' AND question = 'Can I upgrade my plan later?');

