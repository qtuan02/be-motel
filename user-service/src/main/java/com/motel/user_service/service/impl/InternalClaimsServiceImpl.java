package com.motel.user_service.service.impl;

import static sharing.constant.user_service.UserServiceDomainConstant.ACTIVE_DELETED_AT;

import com.motel.user_service.entity.UserBuildingPermission;
import com.motel.user_service.entity.UserProfile;
import com.motel.user_service.repository.UserBuildingPermissionRepository;
import com.motel.user_service.repository.UserProfileRepository;
import com.motel.user_service.service.InternalClaimsService;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sharing.dto.user_service.internal.InternalClaimsResponse;
import sharing.enums.user_service.AuditEventType;
import sharing.enums.user_service.AuditResult;
import sharing.enums.user_service.UserPermission;
import sharing.enums.user_service.UserRole;
import sharing.exception.AppException;
import sharing.exception.user_service.UserServiceErrorCode;

@Service
public class InternalClaimsServiceImpl implements InternalClaimsService {
    private final UserProfileRepository userProfileRepository;
    private final UserBuildingPermissionRepository permissionRepository;
    private final AuthAuditService authAuditService;

    public InternalClaimsServiceImpl(
            UserProfileRepository userProfileRepository,
            UserBuildingPermissionRepository permissionRepository,
            AuthAuditService authAuditService) {
        this.userProfileRepository = userProfileRepository;
        this.permissionRepository = permissionRepository;
        this.authAuditService = authAuditService;
    }

    @Override
    @Transactional(readOnly = true)
    public InternalClaimsResponse getClaimsByKeycloakId(String keycloakId, String requestId) {
        UserProfile profile = userProfileRepository
                .findByKeycloakIdAndDeletedAt(keycloakId, ACTIVE_DELETED_AT)
                .orElseThrow(() -> new AppException(UserServiceErrorCode.USER_PROFILE_NOT_FOUND));
        Set<UUID> assignedBuildings = new LinkedHashSet<>();
        Set<UserPermission> permissions = new LinkedHashSet<>();
        for (UserBuildingPermission permission :
                permissionRepository.findByUserIdAndDeletedAt(profile.getId(), ACTIVE_DELETED_AT)) {
            assignedBuildings.add(permission.getBuildingId());
            permissions.add(permission.getPermission());
        }

        InternalClaimsResponse response = new InternalClaimsResponse();
        response.setSub(profile.getKeycloakId());
        response.setRole(profile.getRole());
        response.setLandlordId(resolveLandlordId(profile));
        response.setAssignedBuildings(assignedBuildings.stream().toList());
        response.setPermissions(permissions.stream().toList());
        response.setSensitivityClearance(profile.getSensitivityClearance());
        response.setIsActive(profile.getIsActive());

        authAuditService.log(
                requestId,
                profile.getId(),
                profile.getId(),
                AuditEventType.INTERNAL_CLAIMS_READ,
                AuditResult.SUCCESS,
                Map.of("keycloakId", keycloakId));

        return response;
    }

    private UUID resolveLandlordId(UserProfile profile) {
        if (profile.getRole() == UserRole.LANDLORD) {
            return profile.getId();
        }
        return profile.getLandlordId();
    }
}
