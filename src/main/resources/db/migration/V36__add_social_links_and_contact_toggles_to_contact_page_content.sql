-- ==========================================================
-- V36__add_social_links_and_contact_toggles_to_contact_page_content.sql
-- Add enable/disable toggles for direct contact channels and social_links to contact_page_content
-- ==========================================================

ALTER TABLE contact_page_content
    ADD COLUMN IF NOT EXISTS direct_contact_email_enabled BOOLEAN DEFAULT true,
    ADD COLUMN IF NOT EXISTS direct_contact_phone_enabled BOOLEAN DEFAULT true,
    ADD COLUMN IF NOT EXISTS direct_contact_location_enabled BOOLEAN DEFAULT true,
    ADD COLUMN IF NOT EXISTS direct_contact_working_hours_enabled BOOLEAN DEFAULT true,
    ADD COLUMN IF NOT EXISTS social_links TEXT DEFAULT '[]';

-- Backfill default values for existing row
UPDATE contact_page_content
SET
    direct_contact_email_enabled = COALESCE(direct_contact_email_enabled, true),
    direct_contact_phone_enabled = COALESCE(direct_contact_phone_enabled, true),
    direct_contact_location_enabled = COALESCE(direct_contact_location_enabled, true),
    direct_contact_working_hours_enabled = COALESCE(direct_contact_working_hours_enabled, true),
    social_links = COALESCE(social_links, '[{"platform":"Instagram","url":"https://instagram.com/designtouchplus","enabled":true},{"platform":"YouTube","url":"https://youtube.com/@designtouchplus","enabled":true},{"platform":"LinkedIn","url":"https://linkedin.com/company/designtouchplus","enabled":true}]')
WHERE id = 1;

