-- ==========================================================
-- V39__update_rohit_sharma_social_urls.sql
-- Set LinkedIn and Instagram URLs for Rohit Sharma
-- ==========================================================

UPDATE team_member
SET linkedin_url = 'https://www.linkedin.com/in/rohitsharma/',
    instagram_url = 'https://www.instagram.com/rohitsharma/'
WHERE name = 'Rohit Sharma' AND (linkedin_url IS NULL OR linkedin_url = '');

