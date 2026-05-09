CREATE TABLE IF NOT EXISTS fcm_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token_ciphertext VARCHAR(512) NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    device_type VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_fcm_tokens_user FOREIGN KEY (user_id) REFERENCES user_profiles(id),
    CONSTRAINT ck_fcm_tokens_device_type CHECK (device_type IN ('ANDROID', 'IOS', 'WEB'))
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_fcm_tokens_user_hash_active
    ON fcm_tokens (user_id, token_hash)
    WHERE deleted_at = 0;

CREATE INDEX IF NOT EXISTS idx_fcm_tokens_user_active
    ON fcm_tokens (user_id)
    WHERE deleted_at = 0;
