-- ==========================================================
-- V20__backfill_tool_local_icon_paths.sql
-- Backfill local frontend SVG asset paths for the 7 canonical
-- tools while preserving all existing tool records and fields.
-- ==========================================================

UPDATE tool SET logo_url = '/assets/tools/premiere-pro.svg'    WHERE (logo_url IS NULL OR logo_url = '') AND (LOWER(icon_key) = 'pr' OR LOWER(name) LIKE '%premiere%');
UPDATE tool SET logo_url = '/assets/tools/after-effects.svg'   WHERE (logo_url IS NULL OR logo_url = '') AND (LOWER(icon_key) = 'ae' OR LOWER(name) LIKE '%after effects%');
UPDATE tool SET logo_url = '/assets/tools/davinci-resolve.svg' WHERE (logo_url IS NULL OR logo_url = '') AND (LOWER(icon_key) = 'dv' OR LOWER(name) LIKE '%davinci%');
UPDATE tool SET logo_url = '/assets/tools/photoshop.svg'       WHERE (logo_url IS NULL OR logo_url = '') AND (LOWER(icon_key) = 'ps' OR LOWER(name) LIKE '%photoshop%');
UPDATE tool SET logo_url = '/assets/tools/illustrator.svg'     WHERE (logo_url IS NULL OR logo_url = '') AND (LOWER(icon_key) = 'ai' OR LOWER(name) LIKE '%illustrator%');
UPDATE tool SET logo_url = '/assets/tools/blender.svg'         WHERE (logo_url IS NULL OR logo_url = '') AND (LOWER(icon_key) = '3d' OR LOWER(name) LIKE '%blender%');
UPDATE tool SET logo_url = '/assets/tools/figma.svg'           WHERE (logo_url IS NULL OR logo_url = '') AND (LOWER(icon_key) = 'fg' OR LOWER(name) LIKE '%figma%');

