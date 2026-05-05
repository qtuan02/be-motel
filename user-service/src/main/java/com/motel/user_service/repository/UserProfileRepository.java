package com.motel.user_service.repository;

import com.motel.user_service.entity.UserProfile;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import sharing.base.repository.BaseRepository;

@Repository
public interface UserProfileRepository extends BaseRepository<UserProfile, UUID> {
    Optional<UserProfile> findByKeycloakId(String keycloakId);

    Optional<UserProfile> findByEmailAndDeletedAt(String email, Long deletedAt);
}
