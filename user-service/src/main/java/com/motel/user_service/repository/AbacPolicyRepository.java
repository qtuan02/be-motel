package com.motel.user_service.repository;

import com.motel.user_service.entity.AbacPolicy;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AbacPolicyRepository extends JpaRepository<AbacPolicy, UUID> {

    @Query("""
        select p from AbacPolicy p
        where p.deletedAt = 0
          and (p.isSystemPolicy = true or p.landlordId = :landlordId)
        order by p.isSystemPolicy desc, p.createdAt desc
        """)
    List<AbacPolicy> findVisiblePolicies(@Param("landlordId") UUID landlordId);

    Optional<AbacPolicy> findByIdAndDeletedAt(UUID id, Long deletedAt);
}
