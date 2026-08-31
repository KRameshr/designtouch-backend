-- ==========================================================
-- V1__create_tables.sql
-- DesignTouch+ initial schema
-- ==========================================================

-- ---------- ENUM TYPES ----------
CREATE TYPE content_type AS ENUM ('VIDEO_EDITING', 'THUMBNAIL_DESIGN');
CREATE TYPE portfolio_category AS ENUM ('YOUTUBE', 'INSTAGRAM_REELS', 'COMMERCIAL', 'MOTION_GRAPHICS', 'CORPORATE', 'REAL_ESTATE');
CREATE TYPE blog_category AS ENUM ('VIDEO_EDITING', 'MOTION_GRAPHICS', 'CONTENT_CREATION', 'YOUTUBE', 'BUSINESS');
CREATE TYPE price_type AS ENUM ('ONE_TIME', 'MONTHLY');
CREATE TYPE page_type AS ENUM ('ABOUT', 'SERVICES', 'PRICING');
CREATE TYPE message_status AS ENUM ('NEW', 'READ', 'REPLIED');
CREATE TYPE faq_category AS ENUM ('PRICING', 'GENERAL');
CREATE TYPE admin_role AS ENUM ('SUPER_ADMIN', 'EDITOR');

-- ---------- CORE CONTENT TABLES ----------

CREATE TABLE team_member (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(150) NOT NULL,
    role            VARCHAR(150) NOT NULL,
    photo_url       TEXT,
    linkedin_url    TEXT,
    instagram_url   TEXT,
    display_order   INTEGER NOT NULL DEFAULT 0,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE portfolio_item (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(200) NOT NULL,
    content_type    content_type NOT NULL,
    category        portfolio_category,
    sub_category    VARCHAR(150),
    description     TEXT,
    duration        VARCHAR(20),
    video_url       TEXT,
    image_url       TEXT,
    thumbnail_url   TEXT,
    display_order   INTEGER NOT NULL DEFAULT 0,
    is_featured     BOOLEAN NOT NULL DEFAULT FALSE,
    view_count      INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE service (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    icon            VARCHAR(100),
    title           VARCHAR(150) NOT NULL,
    description     TEXT,
    features        TEXT[],
    color_theme     VARCHAR(30),
    slug            VARCHAR(150) NOT NULL UNIQUE,
    order_number    VARCHAR(10),
    display_order   INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE pricing_plan (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_name           VARCHAR(100) NOT NULL,
    icon                VARCHAR(100),
    description         TEXT,
    price_type          price_type NOT NULL,
    price               NUMERIC(10, 2),
    currency            VARCHAR(5) DEFAULT '₹',
    unit                VARCHAR(30),
    features            TEXT[],
    is_most_popular     BOOLEAN NOT NULL DEFAULT FALSE,
    cta_text            VARCHAR(50),
    cta_link            VARCHAR(200),
    display_order       INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE testimonial (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quote               TEXT NOT NULL,
    author_name         VARCHAR(150) NOT NULL,
    author_role         VARCHAR(150),
    author_company      VARCHAR(150),
    author_avatar_url   TEXT,
    rating              SMALLINT CHECK (rating BETWEEN 1 AND 5),
    display_order       INTEGER NOT NULL DEFAULT 0,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE client (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_name     VARCHAR(150) NOT NULL,
    logo_url        TEXT NOT NULL,
    website_url     TEXT,
    display_order   INTEGER NOT NULL DEFAULT 0
);

-- ---------- BLOG ----------

CREATE TABLE blog_post (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(250) NOT NULL,
    slug            VARCHAR(250) NOT NULL UNIQUE,
    excerpt         VARCHAR(500),
    content         TEXT NOT NULL,
    cover_image_url TEXT,
    category        blog_category NOT NULL,
    author_id       UUID REFERENCES team_member(id) ON DELETE SET NULL,
    reading_time    INTEGER,
    view_count      INTEGER NOT NULL DEFAULT 0,
    is_published    BOOLEAN NOT NULL DEFAULT TRUE,
    published_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ---------- SIMPLE REFERENCE TABLES ----------

CREATE TABLE stat_item (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    label           VARCHAR(50) NOT NULL,
    value           INTEGER NOT NULL,
    suffix          VARCHAR(10),
    description     VARCHAR(150) NOT NULL,
    icon            VARCHAR(100),
    display_order   INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE company_value (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(100) NOT NULL,
    description     TEXT,
    icon            VARCHAR(100),
    display_order   INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE process_step (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    page_type       page_type NOT NULL,
    step_number     SMALLINT NOT NULL,
    title           VARCHAR(100) NOT NULL,
    description     TEXT,
    icon            VARCHAR(100),
    display_order   INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE tool (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100) NOT NULL,
    icon_key        VARCHAR(100),
    display_order   INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE add_on (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100) NOT NULL,
    icon            VARCHAR(100),
    price_label     VARCHAR(50) NOT NULL,
    display_order   INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE faq (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question        VARCHAR(300) NOT NULL,
    answer          TEXT NOT NULL,
    category        faq_category NOT NULL,
    display_order   INTEGER NOT NULL DEFAULT 0
);

-- ---------- SINGLETON TABLES (exactly one row) ----------

CREATE TABLE about_content (
    id                          SMALLINT PRIMARY KEY DEFAULT 1,
    heading                     VARCHAR(200),
    subheading                  TEXT,
    story_paragraph_1           TEXT,
    story_paragraph_2           TEXT,
    founder_name                VARCHAR(150),
    founder_signature_url       TEXT,
    story_video_url             TEXT,
    story_video_thumbnail_url   TEXT,
    CONSTRAINT single_row_about CHECK (id = 1)
);

CREATE TABLE company_info (
    id                  SMALLINT PRIMARY KEY DEFAULT 1,
    email               VARCHAR(150) NOT NULL,
    phone               VARCHAR(30) NOT NULL,
    whatsapp_number     VARCHAR(30),
    address             VARCHAR(250),
    working_hours       VARCHAR(150),
    map_embed_url       TEXT,
    CONSTRAINT single_row_company CHECK (id = 1)
);

-- ---------- TRANSACTIONAL / USER-SUBMITTED TABLES ----------

CREATE TABLE contact_message (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(150) NOT NULL,
    email           VARCHAR(150) NOT NULL,
    company         VARCHAR(150),
    subject         VARCHAR(250),
    message         TEXT NOT NULL,
    status          message_status NOT NULL DEFAULT 'NEW',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE newsletter_subscriber (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(150) NOT NULL UNIQUE,
    subscribed_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE admin_user (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username        VARCHAR(100) NOT NULL UNIQUE,
    password_hash   TEXT NOT NULL,
    role            admin_role NOT NULL DEFAULT 'EDITOR',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ---------- INDEXES ----------

CREATE INDEX idx_blog_post_slug ON blog_post(slug);
CREATE INDEX idx_blog_post_category ON blog_post(category);
CREATE INDEX idx_blog_post_author ON blog_post(author_id);
CREATE INDEX idx_portfolio_content_category ON portfolio_item(content_type, category);
CREATE INDEX idx_portfolio_featured ON portfolio_item(is_featured);
CREATE INDEX idx_service_slug ON service(slug);
CREATE INDEX idx_pricing_plan_type ON pricing_plan(price_type);
CREATE INDEX idx_faq_category ON faq(category);
CREATE INDEX idx_process_step_page_type ON process_step(page_type);
CREATE INDEX idx_contact_message_status ON contact_message(status);
