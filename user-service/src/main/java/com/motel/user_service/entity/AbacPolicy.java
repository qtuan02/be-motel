package com.motel.user_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import sharing.enums.user_service.PolicyEffect;
import sharing.utils.UUIDv7;

@Getter
@Setter
@Entity
@Table(name = "abac_policies")
public class AbacPolicy {

    @Id
    @UUIDv7
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "landlord_id", columnDefinition = "uuid")
    private UUID landlordId;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "resource", nullable = false, length = 80)
    private String resource;

    @Column(name = "action", nullable = false, length = 80)
    private String action;

    @Enumerated(EnumType.STRING)
    @Column(name = "policy_effect", nullable = false, length = 10)
    private PolicyEffect policyEffect;

    @Column(name = "conditions", nullable = false, columnDefinition = "jsonb")
    private String conditions;

    @Column(name = "is_system_policy", nullable = false)
    private Boolean isSystemPolicy = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at", nullable = false)
    private Long deletedAt = 0L;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (deletedAt == null) {
            deletedAt = 0L;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
