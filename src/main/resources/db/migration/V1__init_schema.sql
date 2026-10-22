-- ============================================================================
-- Flyway Database Migration: V1__init_schema.sql
-- Description: Baseline schema for User Service and Refresh Token Rotation
-- Dialect: PostgreSQL (Enterprise & GDPR-ready standards)
-- ============================================================================

-- 1. Table: users
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

-- Unique index for users email (fast lookups & constraint enforcement)
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email ON users (email);

-- 2. Table: refresh_tokens
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token_hash VARCHAR(255) NOT NULL,
    family_id VARCHAR(255) NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    replaced_by_token VARCHAR(255),
    expiry_date TIMESTAMPTZ NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

-- Indexes for refresh_tokens
CREATE UNIQUE INDEX IF NOT EXISTS ux_refresh_tokens_token_hash ON refresh_tokens (token_hash);
CREATE INDEX IF NOT EXISTS ix_refresh_tokens_family_id ON refresh_tokens (family_id);
CREATE INDEX IF NOT EXISTS ix_refresh_tokens_user_id ON refresh_tokens (user_id);
