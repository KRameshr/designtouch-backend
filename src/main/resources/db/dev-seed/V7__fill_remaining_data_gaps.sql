-- ==========================================================
-- V7__fill_remaining_data_gaps.sql
-- Closes out the remaining backend data gaps flagged during frontend build:
--   - process_step rows for SERVICES and PRICING pages (only ABOUT existed)
--   - 3 more tools (Illustrator, Blender, Figma) to match the Figma 7-tool row
--   - PREMIUM one-time plan (Figma showed 4 plans, V2 only seeded 3)
--   - 4 MONTHLY package plans (the toggle existed but had no data behind it)
--   - 2 more add-ons (Rush Delivery, Script Writing)
-- ==========================================================

-- ---------- Process Steps: SERVICES page ----------
INSERT INTO process_step (page_type, step_number, title, description, display_order) VALUES
    ('SERVICES', 1, '1. DISCOVER', 'We learn about your brand, goals and vision.', 1),
    ('SERVICES', 2, '2. PLAN', 'We brainstorm ideas and create a clear strategy.', 2),
    ('SERVICES', 3, '3. CREATE', 'Our team gets to work and brings your story to life.', 3),
    ('SERVICES', 4, '4. REVIEW', 'We collaborate, refine and make it perfect.', 4),
    ('SERVICES', 5, '5. DELIVER', 'We deliver the final product that drives results.', 5);

-- ---------- Process Steps: PRICING page ----------
INSERT INTO process_step (page_type, step_number, title, description, display_order) VALUES
    ('PRICING', 1, '1. DISCUSS', 'We understand your requirements and goals.', 1),
    ('PRICING', 2, '2. PLAN', 'We plan the best approach for your project.', 2),
    ('PRICING', 3, '3. CREATE', 'Our editors bring your vision to life with creativity.', 3),
    ('PRICING', 4, '4. REVIEW', 'You review the draft and share your feedback.', 4),
    ('PRICING', 5, '5. DELIVER', 'We deliver the final video that exceeds your expectations.', 5);

-- ---------- Remaining Tools ----------
INSERT INTO tool (name, display_order) VALUES
    ('Illustrator', 5),
    ('Blender', 6),
    ('Figma', 7);

-- ---------- PREMIUM one-time plan (between Standard and Enterprise) ----------
INSERT INTO pricing_plan (plan_name, description, price_type, price, unit, features, is_most_popular, cta_text, display_order) VALUES
    ('PREMIUM', 'For brands that want top-notch quality.', 'ONE_TIME', 12999, '/video',
     ARRAY['Up to 20 minutes runtime', 'Cinematic Editing', 'Advanced Color Grading', 'Motion Graphics & VFX', 'Sound Design', 'Unlimited Revisions', '1-2 Days Delivery'],
     false, 'GET STARTED', 3);

-- Nudge Enterprise's display_order so it stays last after Premium is inserted
UPDATE pricing_plan SET display_order = 4 WHERE plan_name = 'ENTERPRISE';

-- ---------- Monthly Packages (fills the "MONTHLY PACKAGES" toggle) ----------
INSERT INTO pricing_plan (plan_name, description, price_type, price, unit, features, is_most_popular, cta_text, display_order) VALUES
    ('BASIC', 'For creators who publish regularly.', 'MONTHLY', 9999, '/month',
     ARRAY['4 videos per month', 'Basic Editing', 'Color Correction', '2 Revisions per video', '3 Days Delivery'],
     false, 'GET STARTED', 1),
    ('STANDARD', 'For growing brands and businesses.', 'MONTHLY', 22999, '/month',
     ARRAY['8 videos per month', 'Advanced Editing', 'Color Grading', 'Motion Graphics', 'Unlimited Revisions', 'Dedicated Editor'],
     true, 'GET STARTED', 2),
    ('PREMIUM', 'For high-volume content teams.', 'MONTHLY', 39999, '/month',
     ARRAY['16 videos per month', 'Cinematic Editing', 'Motion Graphics & VFX', 'Sound Design', 'Priority Support', '1-2 Days Delivery'],
     false, 'GET STARTED', 3),
    ('ENTERPRISE', 'For agencies & businesses with bulk needs.', 'MONTHLY', NULL, NULL,
     ARRAY['Custom video volume', 'Dedicated Account Manager', 'Priority Delivery', 'Unlimited Revisions', 'Ongoing Support'],
     false, 'CONTACT US', 4);

-- ---------- Remaining Add-ons ----------
INSERT INTO add_on (name, price_label, display_order) VALUES
    ('Rush Delivery', '+₹1,000', 4),
    ('Script Writing (Per Video)', '+₹1,500', 5);
