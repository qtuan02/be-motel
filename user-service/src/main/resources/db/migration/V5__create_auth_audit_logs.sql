CREATE TABLE IF NOT EXISTS auth_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    request_id VARCHAR(120) NOT NULL,
    actor_user_id UUID,
    target_user_id UUID,
    event_type VARCHAR(80) NOT NULL,
    result VARCHAR(20) NOT NULL,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_auth_audit_logs_event_type CHECK (
        event_type IN (
            'LANDLORD_REGISTERED',
            'PROFILE_UPDATED',
            'ZALO_UID_UPDATED',
            'FCM_TOKEN_REGISTERED',
            'FCM_TOKEN_REVOKED',
            'STAFF_INVITED',
            'STAFF_PERMISSION_REPLACED',
            'STAFF_DEACTIVATED',
            'ABAC_POLICY_CREATED',
            'ABAC_POLICY_DELETED',
            'INTERNAL_CLAIMS_READ'
        )
    ),
    CONSTRAINT ck_auth_audit_logs_result CHECK (result IN ('SUCCESS', 'DENIED', 'ERROR'))
);

CREATE INDEX IF NOT EXISTS idx_auth_audit_logs_target_user_created_at
    ON auth_audit_logs (target_user_id, created_at DESC);
