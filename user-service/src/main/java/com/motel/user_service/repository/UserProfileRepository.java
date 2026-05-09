package com.motel.user_service.repository;

import com.motel.user_service.entity.UserProfile;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sharing.base.repository.BaseRepository;
import sharing.enums.user_service.UserRole;

@Repository
public interface UserProfileRepository extends BaseRepository<UserProfile, UUID> {
    Optional<UserProfile> findByKeycloakIdAndDeletedAt(String keycloakId, Long deletedAt);

    Optional<UserProfile> findByEmailNormalizedAndDeletedAt(String emailNormalized, Long deletedAt);

    @Query("""
        select u from UserProfile u
        where u.deletedAt = 0
          and u.isActive = true
          and u.landlordId = :landlordId
          and u.role in :roles
        """)
    List<UserProfile> findActiveStaffByLandlordAndRoles(
            @Param("landlordId") UUID landlordId, @Param("roles") Collection<UserRole> roles);

    Optional<UserProfile> findByIdAndDeletedAt(UUID id, Long deletedAt);
}
