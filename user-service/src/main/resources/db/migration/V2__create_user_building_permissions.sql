CREATE TABLE IF NOT EXISTS user_building_permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    building_id UUID NOT NULL,
    permission VARCHAR(50) NOT NULL,
    valid_from DATE,
    valid_until DATE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_user_building_permissions_user FOREIGN KEY (user_id) REFERENCES user_profiles(id),
    CONSTRAINT ck_user_building_permissions_valid_range CHECK (
        valid_until IS NULL OR valid_from IS NULL OR valid_until >= valid_from
    )
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_user_building_permissions_active
    ON user_building_permissions (user_id, building_id)
    WHERE deleted_at = 0;

CREATE INDEX IF NOT EXISTS idx_user_building_permissions_user_active
    ON user_building_permissions (user_id)
    WHERE deleted_at = 0;
