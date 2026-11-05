-- ==========================================================
-- V33__add_direct_contact_to_contact_page_content.sql
-- Add editable Direct Contact information fields to contact_page_content
-- ==========================================================

ALTER TABLE contact_page_content
    ADD COLUMN IF NOT EXISTS direct_contact_eyebrow VARCHAR(150) DEFAULT 'GET IN TOUCH',
    ADD COLUMN IF NOT EXISTS direct_contact_heading VARCHAR(150) DEFAULT 'DIRECT CONTACT',
    ADD COLUMN IF NOT EXISTS direct_contact_description TEXT DEFAULT 'Prefer direct communication? Reach out to our team via email, phone, or WhatsApp.',
    ADD COLUMN IF NOT EXISTS direct_contact_email_label VARCHAR(100) DEFAULT 'EMAIL US',
    ADD COLUMN IF NOT EXISTS direct_contact_email VARCHAR(255) DEFAULT 'hello@designtouchplus.com',
    ADD COLUMN IF NOT EXISTS direct_contact_email_sub VARCHAR(150) DEFAULT 'We''ll reply within 24 hours',
    ADD COLUMN IF NOT EXISTS direct_contact_phone_label VARCHAR(100) DEFAULT 'CALL / WHATSAPP',
    ADD COLUMN IF NOT EXISTS direct_contact_phone VARCHAR(100) DEFAULT '+91 12345 67890',
    ADD COLUMN IF NOT EXISTS direct_contact_phone_sub VARCHAR(150) DEFAULT 'Mon - Sat: 10:00 AM - 7:00 PM',
    ADD COLUMN IF NOT EXISTS direct_contact_location_label VARCHAR(100) DEFAULT 'OUR LOCATION',
    ADD COLUMN IF NOT EXISTS direct_contact_location VARCHAR(255) DEFAULT 'India · Remote Worldwide',
    ADD COLUMN IF NOT EXISTS direct_contact_location_sub VARCHAR(150) DEFAULT 'We work with clients globally',
    ADD COLUMN IF NOT EXISTS direct_contact_working_hours_label VARCHAR(100) DEFAULT 'WORKING HOURS',
    ADD COLUMN IF NOT EXISTS direct_contact_working_hours VARCHAR(255) DEFAULT 'Monday - Saturday, 10:00 AM - 7:00 PM (IST)',
    ADD COLUMN IF NOT EXISTS direct_contact_working_hours_sub VARCHAR(150) DEFAULT '';

-- Backfill default values for existing row
UPDATE contact_page_content
SET
    direct_contact_eyebrow = COALESCE(direct_contact_eyebrow, 'GET IN TOUCH'),
    direct_contact_heading = COALESCE(direct_contact_heading, 'DIRECT CONTACT'),
    direct_contact_description = COALESCE(direct_contact_description, 'Prefer direct communication? Reach out to our team via email, phone, or WhatsApp.'),
    direct_contact_email_label = COALESCE(direct_contact_email_label, 'EMAIL US'),
    direct_contact_email = COALESCE(direct_contact_email, 'hello@designtouchplus.com'),
    direct_contact_email_sub = COALESCE(direct_contact_email_sub, 'We''ll reply within 24 hours'),
    direct_contact_phone_label = COALESCE(direct_contact_phone_label, 'CALL / WHATSAPP'),
    direct_contact_phone = COALESCE(direct_contact_phone, '+91 12345 67890'),
    direct_contact_phone_sub = COALESCE(direct_contact_phone_sub, 'Mon - Sat: 10:00 AM - 7:00 PM'),
    direct_contact_location_label = COALESCE(direct_contact_location_label, 'OUR LOCATION'),
    direct_contact_location = COALESCE(direct_contact_location, 'India · Remote Worldwide'),
    direct_contact_location_sub = COALESCE(direct_contact_location_sub, 'We work with clients globally'),
    direct_contact_working_hours_label = COALESCE(direct_contact_working_hours_label, 'WORKING HOURS'),
    direct_contact_working_hours = COALESCE(direct_contact_working_hours, 'Monday - Saturday, 10:00 AM - 7:00 PM (IST)'),
    direct_contact_working_hours_sub = COALESCE(direct_contact_working_hours_sub, '')
WHERE id = 1;

