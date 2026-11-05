-- V40__create_chat_lead_and_conversations.sql
-- Chatbot lead capture, conversation sessions, and message persistence

CREATE TABLE IF NOT EXISTS chat_lead (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL,
    conversation_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    source VARCHAR(50) NOT NULL DEFAULT 'WEBSITE_CHATBOT',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_chat_lead_email ON chat_lead (LOWER(email));
CREATE INDEX IF NOT EXISTS idx_chat_lead_created_at ON chat_lead (created_at DESC);

CREATE TABLE IF NOT EXISTS chat_conversation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lead_id UUID NOT NULL REFERENCES chat_lead(id) ON DELETE CASCADE,
    title VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_chat_conversation_lead_id ON chat_conversation (lead_id);
CREATE INDEX IF NOT EXISTS idx_chat_conversation_created_at ON chat_conversation (created_at DESC);

CREATE TABLE IF NOT EXISTS chat_message (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES chat_conversation(id) ON DELETE CASCADE,
    sender_type VARCHAR(20) NOT NULL, -- 'VISITOR', 'ASSISTANT', 'SYSTEM'
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_chat_message_conversation_id ON chat_message (conversation_id);
CREATE INDEX IF NOT EXISTS idx_chat_message_created_at ON chat_message (created_at ASC);

