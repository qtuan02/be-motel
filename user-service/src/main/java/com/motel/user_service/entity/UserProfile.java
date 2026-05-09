package com.motel.user_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import sharing.base.entity.BaseEntity;
import sharing.constants.UserSerivceConstant;

@Getter
@Setter
@Entity
@Table(name = UserSerivceConstant.USER_PROFILES_TABLE)
public class UserProfile extends BaseEntity {
    @Column(name = "keycloak_id", nullable = false)
    private String keycloakId;

    @Column(name = "landlord_id")
    private UUID landlordId;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "zalo_uid")
    private String zaloUid;

    @Column(name = "sensitivity_clearance", nullable = false)
    private String sensitivityClearance = "STANDARD";

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
}
