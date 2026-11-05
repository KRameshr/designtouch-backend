-- V41__remove_chat_conversations_and_messages.sql
-- Remove persistent chat conversation and message storage while preserving visitor Name & Email lead records.

DROP TABLE IF EXISTS chat_message CASCADE;
DROP TABLE IF EXISTS chat_conversation CASCADE;

-- Ensure chat_lead has proper indexes for search and filtering
CREATE INDEX IF NOT EXISTS idx_chat_lead_status ON chat_lead (conversation_status);

