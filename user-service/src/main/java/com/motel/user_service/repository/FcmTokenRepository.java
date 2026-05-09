package com.motel.user_service.repository;

import com.motel.user_service.entity.FcmToken;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, UUID> {
    Optional<FcmToken> findByUserIdAndTokenHashAndDeletedAt(UUID userId, String tokenHash, Long deletedAt);

    List<FcmToken> findByUserIdAndDeletedAt(UUID userId, Long deletedAt);

    Optional<FcmToken> findByIdAndUserIdAndDeletedAt(UUID id, UUID userId, Long deletedAt);
}
