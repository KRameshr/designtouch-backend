-- ==========================================================
-- V2__seed_sample_data.sql
-- Sample data for local testing. Safe to run in dev; delete rows
-- manually (or write a V3 migration) before going live with real content.
-- ==========================================================

-- ---------- Company Info (singleton) ----------
INSERT INTO company_info (id, email, phone, whatsapp_number, address, working_hours, map_embed_url)
VALUES (1, 'hello@designtouchplus.com', '+91 12345 67890', '+91 12345 67890',
        'India · Remote Worldwide', 'Monday - Saturday, 10:00 AM - 7:00 PM (IST)', NULL);

-- ---------- About Content (singleton) ----------
INSERT INTO about_content (id, heading, subheading, story_paragraph_1, story_paragraph_2, founder_name)
VALUES (1, 'WE ARE DESIGNTOUCH+',
        'We are a team of passionate storytellers, editors and creators who turn ideas into powerful visual experiences.',
        'DesignTouch+ was born out of a love for storytelling and a passion for visual creativity.',
        'We believe every brand has a story worth telling, and our job is to tell it the right way.',
        'Founder, DesignTouch+');

-- ---------- Team Members ----------
INSERT INTO team_member (name, role, display_order) VALUES
    ('Rohit Sharma', 'Founder & Creative Director', 1),
    ('Arjun Verma', 'Head of Video Production', 2),
    ('Pooja Nair', 'Motion Graphics Designer', 3);

-- ---------- Stats ----------
INSERT INTO stat_item (label, value, suffix, description, display_order) VALUES
    ('150', 150, '+', 'Projects Completed', 1),
    ('80', 80, '+', 'Happy Clients', 2),
    ('4', 4, '+', 'Years Experience', 3),
    ('10', 10, '+', 'Countries Served', 4);

-- ---------- Company Values ----------
INSERT INTO company_value (title, description, display_order) VALUES
    ('Quality First', 'We never compromise on quality. Every frame we deliver is crafted with perfection.', 1),
    ('Creativity', 'Creativity is at the heart of everything we do.', 2),
    ('On-Time Delivery', 'Time is valuable. We respect deadlines and always deliver on time, every time.', 3),
    ('Client First', 'Our clients'' success is our success.', 4);

-- ---------- Process Steps (About page) ----------
INSERT INTO process_step (page_type, step_number, title, description, display_order) VALUES
    ('ABOUT', 1, '1. DISCOVER', 'We learn about your brand, goals and vision.', 1),
    ('ABOUT', 2, '2. PLAN', 'We brainstorm ideas and create a clear strategy.', 2),
    ('ABOUT', 3, '3. CREATE', 'Our team gets to work and brings your story to life.', 3),
    ('ABOUT', 4, '4. REVIEW', 'We collaborate, refine and make it perfect.', 4),
    ('ABOUT', 5, '5. DELIVER', 'We deliver the final product that drives results.', 5);

-- ---------- Tools ----------
INSERT INTO tool (name, display_order) VALUES
    ('Adobe Premiere Pro', 1),
    ('After Effects', 2),
    ('DaVinci Resolve', 3),
    ('Photoshop', 4);

-- ---------- Services ----------
INSERT INTO service (icon, title, description, features, color_theme, slug, order_number, display_order) VALUES
    ('video', 'Video Editing', 'Engaging edits for YouTube, social media, ads, podcasts and more.',
     ARRAY['Long-form Editing', 'Short-form / Reels', 'Podcast Editing', 'Color Grading'],
     'red', 'video-editing', '01', 1),
    ('motion', 'Motion Graphics', 'Stunning motion graphics that explain, engage and elevate your message.',
     ARRAY['2D/3D Animation', 'Explainer Videos', 'Logo Animation', 'Kinetic Typography'],
     'green', 'motion-graphics', '02', 2);

-- ---------- Clients ----------
INSERT INTO client (client_name, logo_url, display_order) VALUES
    ('Groww', 'https://placehold.co/120x40?text=Groww', 1),
    ('Syska', 'https://placehold.co/120x40?text=Syska', 2),
    ('boAt', 'https://placehold.co/120x40?text=boAt', 3);

-- ---------- Testimonials ----------
INSERT INTO testimonial (quote, author_name, author_role, author_company, rating, display_order) VALUES
    ('DesignTouch+ transformed our raw footage into a compelling story. Their attention to detail and creativity is unmatched.',
     'Rohit Sharma', 'Marketing Head', 'Groww', 5, 1);

-- ---------- Portfolio Items ----------
INSERT INTO portfolio_item (title, content_type, category, description, duration, video_url, is_featured, display_order) VALUES
    ('Motivation Video Edit', 'VIDEO_EDITING', 'YOUTUBE', 'Cinematic editing, sound design and color grading.', '08:12', NULL, true, 1),
    ('Product Advertisement', 'VIDEO_EDITING', 'COMMERCIAL', 'High-impact product ad for social media.', '00:45', NULL, true, 2),
    ('Luxury Villa Promo', 'VIDEO_EDITING', 'REAL_ESTATE', 'Highlighting architecture with cinematic visuals.', '01:15', NULL, true, 3);

-- ---------- Pricing Plans ----------
INSERT INTO pricing_plan (plan_name, description, price_type, price, unit, features, is_most_popular, cta_text, display_order) VALUES
    ('BASIC', 'Perfect for creators and small projects.', 'ONE_TIME', 2999, '/video',
     ARRAY['Up to 5 minutes runtime', 'Basic Editing', 'Color Correction', '2 Revisions', '3 Days Delivery'],
     false, 'GET STARTED', 1),
    ('STANDARD', 'Great for businesses and regular content.', 'ONE_TIME', 6999, '/video',
     ARRAY['Up to 10 minutes runtime', 'Advanced Editing', 'Color Grading', 'Motion Graphics', 'Unlimited Revisions'],
     true, 'GET STARTED', 2),
    ('ENTERPRISE', 'For agencies & businesses with bulk needs.', 'ONE_TIME', NULL, NULL,
     ARRAY['Custom Requirements', 'Dedicated Editor', 'Priority Delivery', 'Unlimited Revisions'],
     false, 'CONTACT US', 3);

-- ---------- Add-ons ----------
INSERT INTO add_on (name, price_label, display_order) VALUES
    ('Additional Minute', '+₹300/min', 1),
    ('Thumbnail Design', '+₹500', 2),
    ('Subtitles (Per Video)', '+₹500', 3);

-- ---------- FAQs ----------
INSERT INTO faq (question, answer, category, display_order) VALUES
    ('What information do you need to get started?', 'Just your raw footage and a brief on your goals — we handle the rest.', 'PRICING', 1),
    ('Do you offer revisions?', 'Yes, every plan includes revisions — see plan details for exact limits.', 'PRICING', 2),
    ('How soon can you start my project?', 'We typically start within 1-2 business days of confirming your brief.', 'GENERAL', 1),
    ('Do you sign NDAs for projects?', 'Yes, we''re happy to sign an NDA for any project that requires one.', 'GENERAL', 2);

-- ---------- Blog Post ----------
INSERT INTO blog_post (title, slug, excerpt, content, category, reading_time) VALUES
    ('10 Video Editing Tips That Instantly Improve Quality', '10-video-editing-tips',
     'Simple yet powerful tips that can take your edits from good to professional.',
     'Full article content goes here — this is placeholder body text for local testing.',
     'VIDEO_EDITING', 5);
