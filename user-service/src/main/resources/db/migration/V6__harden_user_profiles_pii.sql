ALTER TABLE user_profiles
    DROP CONSTRAINT IF EXISTS user_profiles_email_key;

ALTER TABLE user_profiles
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS email_normalized VARCHAR(200),
    ADD COLUMN IF NOT EXISTS phone_ciphertext VARCHAR(512),
    ADD COLUMN IF NOT EXISTS phone_hash VARCHAR(64),
    ADD COLUMN IF NOT EXISTS phone_masked VARCHAR(30),
    ADD COLUMN IF NOT EXISTS zalo_uid_ciphertext VARCHAR(512),
    ADD COLUMN IF NOT EXISTS zalo_uid_hash VARCHAR(64);

UPDATE user_profiles
SET email_normalized = LOWER(TRIM(email))
WHERE email_normalized IS NULL;

ALTER TABLE user_profiles
    ALTER COLUMN email_normalized SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_user_profiles_email_active
    ON user_profiles (email_normalized)
    WHERE deleted_at = 0;

DO
$$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_user_profiles_role') THEN
        ALTER TABLE user_profiles
            ADD CONSTRAINT ck_user_profiles_role CHECK (role IN ('LANDLORD', 'MANAGER', 'ACCOUNTANT', 'TENANT', 'SYSTEM'));
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_user_profiles_sensitivity_clearance') THEN
        ALTER TABLE user_profiles
            ADD CONSTRAINT ck_user_profiles_sensitivity_clearance CHECK (sensitivity_clearance IN ('STANDARD', 'ELEVATED'));
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_user_building_permissions_permission') THEN
        ALTER TABLE user_building_permissions
            ADD CONSTRAINT ck_user_building_permissions_permission CHECK (permission IN ('MANAGE_ROOM', 'MANAGE_BILLING'));
    END IF;
END
$$;
