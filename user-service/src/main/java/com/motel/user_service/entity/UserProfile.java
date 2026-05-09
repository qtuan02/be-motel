package com.motel.user_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import sharing.base.entity.BaseEntity;
import sharing.enums.user_service.SensitivityClearance;
import sharing.enums.user_service.UserRole;

@Getter
@Setter
@Entity
@Table(name = "user_profiles")
public class UserProfile extends BaseEntity {

    @Column(name = "keycloak_id", nullable = false, length = 100)
    private String keycloakId;

    @Column(name = "landlord_id")
    private UUID landlordId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(name = "email", nullable = false, length = 200)
    private String email;

    @Column(name = "email_normalized", nullable = false, length = 200)
    private String emailNormalized;

    @Column(name = "phone_ciphertext", length = 512)
    private String phoneCiphertext;

    @Column(name = "phone_hash", length = 64)
    private String phoneHash;

    @Column(name = "phone_masked", length = 30)
    private String phoneMasked;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "zalo_uid_ciphertext", length = 512)
    private String zaloUidCiphertext;

    @Column(name = "zalo_uid_hash", length = 64)
    private String zaloUidHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "sensitivity_clearance", nullable = false, length = 20)
    private SensitivityClearance sensitivityClearance = SensitivityClearance.STANDARD;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
}
