-- Add revoked tokens table (Refresh Token Rotation / JWT Revocation support)
CREATE TABLE IF NOT EXISTS revoked_tokens (
    id BIGSERIAL PRIMARY KEY,
    jti VARCHAR(64) NOT NULL UNIQUE,
    expiry_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ NOT NULL
);

-- Indexes (match JPA @Table(indexes=...))
CREATE INDEX IF NOT EXISTS idx_revoked_tokens_expiry ON revoked_tokens (expiry_at);
-- Unique index on jti is already ensured by UNIQUE constraint, but keeping name parity is OK:
-- CREATE UNIQUE INDEX IF NOT EXISTS idx_revoked_tokens_jti ON revoked_tokens (jti);
