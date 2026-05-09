package com.motel.user_service.service.impl;

import static sharing.constant.user_service.UserServiceDomainConstant.ACTIVE_DELETED_AT;

import com.motel.user_service.adapter.IdentityProviderClient;
import com.motel.user_service.auth.ActorContext;
import com.motel.user_service.entity.FcmToken;
import com.motel.user_service.entity.UserBuildingPermission;
import com.motel.user_service.entity.UserProfile;
import com.motel.user_service.repository.FcmTokenRepository;
import com.motel.user_service.repository.UserBuildingPermissionRepository;
import com.motel.user_service.repository.UserProfileRepository;
import com.motel.user_service.service.LandlordStaffService;
import com.motel.user_service.service.UserProfileAccessService;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import sharing.dto.user_service.landlord_staff.InviteStaffRequest;
import sharing.dto.user_service.landlord_staff.ReplaceStaffPermissionsRequest;
import sharing.dto.user_service.landlord_staff.StaffBuildingPermissionInput;
import sharing.dto.user_service.landlord_staff.StaffResponse;
import sharing.enums.user_service.AuditEventType;
import sharing.enums.user_service.AuditResult;
import sharing.enums.user_service.UserRole;
import sharing.exception.AppException;
import sharing.exception.user_service.UserServiceErrorCode;

@Service
public class LandlordStaffServiceImpl implements LandlordStaffService {
    private final UserProfileAccessService userProfileAccessService;
    private final UserProfileRepository userProfileRepository;
    private final UserBuildingPermissionRepository permissionRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final IdentityProviderClient identityProviderClient;
    private final UserProfilePiiUpdater userProfilePiiUpdater;
    private final UserCodeGenerator userCodeGenerator;
    private final AuthAuditService authAuditService;
    private final UserSummaryAssembler userSummaryAssembler;
    private final TransactionTemplate transactionTemplate;

    public LandlordStaffServiceImpl(
            UserProfileAccessService userProfileAccessService,
            UserProfileRepository userProfileRepository,
            UserBuildingPermissionRepository permissionRepository,
            FcmTokenRepository fcmTokenRepository,
            IdentityProviderClient identityProviderClient,
            UserProfilePiiUpdater userProfilePiiUpdater,
            UserCodeGenerator userCodeGenerator,
            AuthAuditService authAuditService,
            UserSummaryAssembler userSummaryAssembler,
            TransactionTemplate transactionTemplate) {
        this.userProfileAccessService = userProfileAccessService;
        this.userProfileRepository = userProfileRepository;
        this.permissionRepository = permissionRepository;
        this.fcmTokenRepository = fcmTokenRepository;
        this.identityProviderClient = identityProviderClient;
        this.userProfilePiiUpdater = userProfilePiiUpdater;
        this.userCodeGenerator = userCodeGenerator;
        this.authAuditService = authAuditService;
        this.userSummaryAssembler = userSummaryAssembler;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffResponse> listStaff(ActorContext actorContext, UUID landlordId) {
        ensureLandlordScope(actorContext, landlordId);
        List<UserProfile> staff = userProfileRepository.findActiveStaffByLandlordAndRoles(
                landlordId, List.of(UserRole.MANAGER, UserRole.ACCOUNTANT));
        return userSummaryAssembler.toSummaryList(staff).stream()
                .map(summary -> {
                    StaffResponse response = new StaffResponse();
                    response.setProfile(summary);
                    return response;
                })
                .toList();
    }

    @Override
    public StaffResponse inviteStaff(ActorContext actorContext, UUID landlordId, InviteStaffRequest request) {
        UserProfile actor = ensureLandlordScope(actorContext, landlordId);
        validateStaffRole(request.getRole());

        String emailNormalized = normalizeEmail(request.getEmail());
        if (userProfileRepository
                .findByEmailNormalizedAndDeletedAt(emailNormalized, ACTIVE_DELETED_AT)
                .isPresent()) {
            throw new AppException(UserServiceErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }

        String externalUserId =
                identityProviderClient.createUser(request.getEmail(), request.getPassword(), request.getFullName());

        UserProfile saved = transactionTemplate.execute(status -> {
            UserProfile staff = new UserProfile();
            staff.setCode(userCodeGenerator.nextCode());
            staff.setKeycloakId(externalUserId);
            staff.setLandlordId(landlordId);
            staff.setRole(request.getRole());
            staff.setFullName(request.getFullName().trim());
            staff.setEmail(request.getEmail().trim());
            staff.setEmailNormalized(emailNormalized);
            staff.setAvatarUrl(request.getAvatarUrl());
            userProfilePiiUpdater.applyPhone(staff, request.getPhone());

            UserProfile created = userProfileRepository.save(staff);
            savePermissions(created.getId(), request.getPermissions());
            authAuditService.log(
                    actorContext.getRequestId(),
                    actor.getId(),
                    created.getId(),
                    AuditEventType.STAFF_INVITED,
                    AuditResult.SUCCESS,
                    Map.of("role", created.getRole().name(), "landlordId", landlordId));
            return created;
        });
        if (saved == null) {
            throw new IllegalStateException("Failed to persist staff profile");
        }
        return toStaffResponse(saved);
    }

    @Override
    @Transactional
    public StaffResponse replacePermissions(
            ActorContext actorContext, UUID landlordId, UUID staffId, ReplaceStaffPermissionsRequest request) {
        UserProfile actor = ensureLandlordScope(actorContext, landlordId);
        UserProfile staff = getOwnedStaff(landlordId, staffId);

        List<UserBuildingPermission> existing =
                permissionRepository.findByUserIdAndDeletedAt(staffId, ACTIVE_DELETED_AT);
        Map<String, UserBuildingPermission> existingByKey = new HashMap<>();
        for (UserBuildingPermission permission : existing) {
            existingByKey.put(permissionKey(permission.getBuildingId()), permission);
        }

        Map<String, StaffBuildingPermissionInput> requestedByKey = new HashMap<>();
        for (StaffBuildingPermissionInput input : request.getPermissions()) {
            validateDateRange(input);
            String key = permissionKey(input.getBuildingId());
            if (requestedByKey.containsKey(key)) {
                throw new AppException(UserServiceErrorCode.STAFF_PERMISSION_DUPLICATE_BUILDING);
            }
            requestedByKey.put(key, input);
        }

        long now = System.currentTimeMillis();

        List<UserBuildingPermission> toSave = new java.util.ArrayList<>();
        for (UserBuildingPermission current : existing) {
            String key = permissionKey(current.getBuildingId());
            if (!requestedByKey.containsKey(key)) {
                current.setDeletedAt(now);
                toSave.add(current);
            }
        }

        for (Map.Entry<String, StaffBuildingPermissionInput> entry : requestedByKey.entrySet()) {
            UserBuildingPermission current = existingByKey.get(entry.getKey());
            StaffBuildingPermissionInput input = entry.getValue();
            if (current == null) {
                UserBuildingPermission created = new UserBuildingPermission();
                created.setUserId(staffId);
                created.setBuildingId(input.getBuildingId());
                created.setPermission(input.getPermission());
                created.setValidFrom(input.getValidFrom());
                created.setValidUntil(input.getValidUntil());
                created.setDeletedAt(ACTIVE_DELETED_AT);
                toSave.add(created);
            } else {
                current.setPermission(input.getPermission());
                current.setValidFrom(input.getValidFrom());
                current.setValidUntil(input.getValidUntil());
                current.setDeletedAt(ACTIVE_DELETED_AT);
                toSave.add(current);
            }
        }

        permissionRepository.saveAll(toSave);

        Map<String, Object> payload = Map.of(
                "staffId", staffId,
                "permissionCount", requestedByKey.size(),
                "landlordId", landlordId);

        authAuditService.log(
                actorContext.getRequestId(),
                actor.getId(),
                staffId,
                AuditEventType.STAFF_PERMISSION_REPLACED,
                AuditResult.SUCCESS,
                payload);

        return toStaffResponse(staff);
    }

    @Override
    public void deactivateStaff(ActorContext actorContext, UUID landlordId, UUID staffId) {
        UserProfile actor = ensureLandlordScope(actorContext, landlordId);
        UserProfile staff = getOwnedStaff(landlordId, staffId);

        identityProviderClient.disableUser(staff.getKeycloakId());
        transactionTemplate.executeWithoutResult(status -> {
            staff.setIsActive(false);
            userProfileRepository.save(staff);

            long now = System.currentTimeMillis();
            List<UserBuildingPermission> permissions =
                    permissionRepository.findByUserIdAndDeletedAt(staffId, ACTIVE_DELETED_AT);
            for (UserBuildingPermission permission : permissions) {
                permission.setDeletedAt(now);
            }
            permissionRepository.saveAll(permissions);

            List<FcmToken> tokens = fcmTokenRepository.findByUserIdAndDeletedAt(staffId, ACTIVE_DELETED_AT);
            for (FcmToken token : tokens) {
                token.setIsActive(false);
                token.setDeletedAt(now);
            }
            fcmTokenRepository.saveAll(tokens);

            authAuditService.log(
                    actorContext.getRequestId(),
                    actor.getId(),
                    staffId,
                    AuditEventType.STAFF_DEACTIVATED,
                    AuditResult.SUCCESS,
                    Map.of("landlordId", landlordId));
        });
    }

    private UserProfile ensureLandlordScope(ActorContext actorContext, UUID landlordId) {
        UserProfile actor = userProfileAccessService.getRequiredActiveByKeycloakId(actorContext.getKeycloakId());
        if (actor.getRole() != UserRole.LANDLORD || !Objects.equals(actor.getId(), landlordId)) {
            throw new AppException(UserServiceErrorCode.LANDLORD_ACCESS_DENIED);
        }
        return actor;
    }

    private void savePermissions(UUID userId, Collection<StaffBuildingPermissionInput> inputs) {
        Set<String> uniqueKeys = new HashSet<>();
        List<UserBuildingPermission> permissions = new java.util.ArrayList<>();
        for (StaffBuildingPermissionInput input : inputs) {
            validateDateRange(input);
            String key = permissionKey(input.getBuildingId());
            if (!uniqueKeys.add(key)) {
                throw new AppException(UserServiceErrorCode.STAFF_PERMISSION_DUPLICATE_BUILDING);
            }
            UserBuildingPermission permission = new UserBuildingPermission();
            permission.setUserId(userId);
            permission.setBuildingId(input.getBuildingId());
            permission.setPermission(input.getPermission());
            permission.setValidFrom(input.getValidFrom());
            permission.setValidUntil(input.getValidUntil());
            permission.setDeletedAt(ACTIVE_DELETED_AT);
            permissions.add(permission);
        }
        permissionRepository.saveAll(permissions);
    }

    private UserProfile getOwnedStaff(UUID landlordId, UUID staffId) {
        UserProfile staff = userProfileRepository
                .findByIdAndDeletedAt(staffId, ACTIVE_DELETED_AT)
                .orElseThrow(() -> new AppException(UserServiceErrorCode.STAFF_NOT_FOUND));
        validateStaffRole(staff.getRole());
        if (!Objects.equals(staff.getLandlordId(), landlordId)) {
            throw new AppException(UserServiceErrorCode.LANDLORD_ACCESS_DENIED);
        }
        return staff;
    }

    private void validateStaffRole(UserRole role) {
        if (role != UserRole.MANAGER && role != UserRole.ACCOUNTANT) {
            throw new AppException(UserServiceErrorCode.STAFF_ROLE_INVALID);
        }
    }

    private void validateDateRange(StaffBuildingPermissionInput input) {
        if (input.getValidFrom() != null
                && input.getValidUntil() != null
                && input.getValidUntil().isBefore(input.getValidFrom())) {
            throw new AppException(UserServiceErrorCode.PERMISSION_DATE_RANGE_INVALID);
        }
    }

    private String permissionKey(UUID buildingId) {
        return buildingId.toString();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private StaffResponse toStaffResponse(UserProfile profile) {
        StaffResponse response = new StaffResponse();
        response.setProfile(userSummaryAssembler.toSummary(profile));
        return response;
    }
}
