-- Track confirmation email dispatch status for newsletter subscribers
ALTER TABLE newsletter_subscriber
ADD COLUMN IF NOT EXISTS confirmation_sent BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS confirmation_sent_at TIMESTAMPTZ;

