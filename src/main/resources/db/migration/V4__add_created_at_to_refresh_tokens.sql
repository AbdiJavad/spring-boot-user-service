-- Add created_at column to refresh_tokens table for token lifecycle tracking
ALTER TABLE refresh_tokens 
ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;
