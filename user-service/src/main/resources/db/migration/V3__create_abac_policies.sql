CREATE TABLE IF NOT EXISTS abac_policies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    landlord_id UUID,
    name VARCHAR(120) NOT NULL,
    resource VARCHAR(80) NOT NULL,
    action VARCHAR(80) NOT NULL,
    policy_effect VARCHAR(10) NOT NULL,
    conditions JSONB NOT NULL,
    is_system_policy BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_abac_policies_policy_effect CHECK (policy_effect IN ('ALLOW', 'DENY'))
);

CREATE INDEX IF NOT EXISTS idx_abac_policies_visible_active
    ON abac_policies (landlord_id, is_system_policy)
    WHERE deleted_at = 0;
