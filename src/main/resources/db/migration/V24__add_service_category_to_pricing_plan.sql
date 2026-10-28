-- V24: Add service_category to pricing_plan to support multiple service categories (VIDEO_EDITING, THUMBNAIL_DESIGN, CONTENT_CREATOR)

ALTER TABLE pricing_plan 
ADD COLUMN IF NOT EXISTS service_category VARCHAR(50) NOT NULL DEFAULT 'VIDEO_EDITING';

CREATE INDEX IF NOT EXISTS idx_pricing_plan_service_category 
ON pricing_plan(service_category);

CREATE INDEX IF NOT EXISTS idx_pricing_plan_service_category_type 
ON pricing_plan(service_category, price_type);

-- Backfill any existing records to VIDEO_EDITING
UPDATE pricing_plan 
SET service_category = 'VIDEO_EDITING' 
WHERE service_category IS NULL;

-- Initial starter draft plans for THUMBNAIL_DESIGN (marked inactive/draft until approved & activated by administrator)
INSERT INTO pricing_plan (
    plan_name, service_category, description, price_type, price, currency, unit, features, is_most_popular, badge_text, accent_color, cta_text, cta_link, display_order, is_active
) VALUES
    ('BASIC', 'THUMBNAIL_DESIGN', 'High-CTR YouTube thumbnail for individual videos.', 'ONE_TIME', 499, '₹', '/thumbnail',
     ARRAY['1 Custom Thumbnail Concept', 'High-Resolution 1080p Export', 'Source PSD Included', '2 Revisions', '24h Delivery'],
     false, NULL, 'green', 'GET STARTED', '/contact', 1, false),
    ('STANDARD', 'THUMBNAIL_DESIGN', 'A/B test ready thumbnail set with retention-focused typography.', 'ONE_TIME', 999, '₹', '/2 thumbnails',
     ARRAY['2 Thumbnail Concepts', 'A/B Testing Variants', 'CTR Strategy Optimization', 'Unlimited Revisions', '24h Priority Delivery'],
     true, 'MOST POPULAR', 'red', 'GET STARTED', '/contact', 2, false),
    ('MONTHLY BUNDLE', 'THUMBNAIL_DESIGN', 'Monthly thumbnail production for active creators.', 'MONTHLY', 4999, '₹', '/month',
     ARRAY['8 Thumbnails / Month', 'Dedicated Thumbnail Artist', 'Channel Visual Consistency', 'Unlimited Revisions', '24h Turnaround'],
     false, NULL, 'violet', 'GET STARTED', '/contact', 3, false),
    ('AGENCY BULK', 'THUMBNAIL_DESIGN', 'Volume thumbnail production for agencies and studios.', 'MONTHLY', NULL, '₹', NULL,
     ARRAY['Unlimited Monthly Volume', 'Multiple Channel Styles', 'Dedicated Art Director', 'Private Slack/Discord Channel'],
     false, NULL, 'orange', 'CONTACT US', '/contact', 4, false);

-- Initial starter draft plans for CONTENT_CREATOR (marked inactive/draft until approved & activated by administrator)
INSERT INTO pricing_plan (
    plan_name, service_category, description, price_type, price, currency, unit, features, is_most_popular, badge_text, accent_color, cta_text, cta_link, display_order, is_active
) VALUES
    ('CREATOR STARTER', 'CONTENT_CREATOR', 'Essential editing and graphics package for rising creators.', 'MONTHLY', 9999, '₹', '/month',
     ARRAY['4 Long-Form Edits / Month', '4 Matching Thumbnails', 'Sound Design & Grading', '2 Revisions / Video'],
     false, NULL, 'green', 'GET STARTED', '/contact', 1, false),
    ('CREATOR PRO', 'CONTENT_CREATOR', 'Complete video and shorts production suite for consistent channels.', 'MONTHLY', 19999, '₹', '/month',
     ARRAY['8 Long-Form Videos / Month', '12 Viral Shorts / Reels', '8 Custom Thumbnails', 'Motion Graphics', 'Unlimited Revisions'],
     true, 'POPULAR PACKAGE', 'red', 'GET STARTED', '/contact', 2, false),
    ('SHORTS & REELS ONLY', 'CONTENT_CREATOR', 'High-retention vertical video production for Instagram, TikTok, and YouTube.', 'ONE_TIME', 4999, '₹', '/pack of 5',
     ARRAY['5 Short-Form Edits', 'Dynamic Captions & SFX', 'Pacing & Retention Hooks', '2 Revisions'],
     false, NULL, 'violet', 'GET STARTED', '/contact', 3, false),
    ('FULL CHANNEL MANAGEMENT', 'CONTENT_CREATOR', 'End-to-end production, SEO, publishing and growth management.', 'MONTHLY', NULL, '₹', NULL,
     ARRAY['Full Production & Editing', 'Thumbnails & Titles', 'Channel Optimization & SEO', 'Dedicated Account Manager'],
     false, NULL, 'orange', 'CONTACT US', '/contact', 4, false);

