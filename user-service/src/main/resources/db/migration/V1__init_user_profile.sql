CREATE TABLE IF NOT EXISTS user_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    keycloak_id VARCHAR(100) UNIQUE NOT NULL,
    landlord_id UUID REFERENCES user_profiles(id),
    role VARCHAR(20) NOT NULL,
    full_name VARCHAR(200) NOT NULL,
    email VARCHAR(200) UNIQUE NOT NULL,
    phone VARCHAR(200),
    avatar_url VARCHAR(500),
    zalo_uid VARCHAR(100),
    sensitivity_clearance VARCHAR(20) NOT NULL DEFAULT 'STANDARD',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at BIGINT NOT NULL DEFAULT 0,
    code VARCHAR(50) NOT NULL
);

CREATE INDEX idx_users_keycloak ON user_profiles(keycloak_id);
CREATE INDEX idx_users_landlord ON user_profiles(landlord_id) WHERE deleted_at = 0;
CREATE INDEX idx_users_email ON user_profiles(email) WHERE deleted_at = 0;
