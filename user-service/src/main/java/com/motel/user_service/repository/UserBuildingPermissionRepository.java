package com.motel.user_service.repository;

import com.motel.user_service.entity.UserBuildingPermission;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBuildingPermissionRepository extends JpaRepository<UserBuildingPermission, UUID> {
    List<UserBuildingPermission> findByUserIdAndDeletedAt(UUID userId, Long deletedAt);

    List<UserBuildingPermission> findByUserIdInAndDeletedAt(Collection<UUID> userIds, Long deletedAt);
}
