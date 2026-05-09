package com.motel.user_service.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.motel.user_service.adapter.IdentityProviderClient;
import com.motel.user_service.auth.ActorContext;
import com.motel.user_service.entity.UserProfile;
import com.motel.user_service.repository.FcmTokenRepository;
import com.motel.user_service.repository.UserBuildingPermissionRepository;
import com.motel.user_service.repository.UserProfileRepository;
import com.motel.user_service.service.UserProfileAccessService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import sharing.dto.user_service.landlord_staff.InviteStaffRequest;
import sharing.dto.user_service.landlord_staff.ReplaceStaffPermissionsRequest;
import sharing.dto.user_service.landlord_staff.StaffBuildingPermissionInput;
import sharing.enums.user_service.UserPermission;
import sharing.enums.user_service.UserRole;
import sharing.exception.AppException;
import sharing.exception.user_service.UserServiceErrorCode;

@ExtendWith(MockitoExtension.class)
class LandlordStaffServiceImplTest {

    @Mock
    private UserProfileAccessService userProfileAccessService;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserBuildingPermissionRepository permissionRepository;

    @Mock
    private FcmTokenRepository fcmTokenRepository;

    @Mock
    private IdentityProviderClient identityProviderClient;

    @Mock
    private UserProfilePiiUpdater userProfilePiiUpdater;

    @Mock
    private UserCodeGenerator userCodeGenerator;

    @Mock
    private AuthAuditService authAuditService;

    @Mock
    private UserSummaryAssembler userSummaryAssembler;

    @Mock
    private PlatformTransactionManager transactionManager;

    private LandlordStaffServiceImpl landlordStaffService;

    @BeforeEach
    void setUp() {
        landlordStaffService = new LandlordStaffServiceImpl(
                userProfileAccessService,
                userProfileRepository,
                permissionRepository,
                fcmTokenRepository,
                identityProviderClient,
                userProfilePiiUpdater,
                userCodeGenerator,
                authAuditService,
                userSummaryAssembler,
                new TransactionTemplate(transactionManager));
    }

    @Test
    void shouldRejectInviteWhenRoleIsNotManagerOrAccountant() {
        UUID landlordId = UUID.randomUUID();
        UserProfile actor = new UserProfile();
        actor.setId(landlordId);
        actor.setRole(UserRole.LANDLORD);
        actor.setIsActive(true);

        when(userProfileAccessService.getRequiredActiveByKeycloakId("kc-landlord"))
                .thenReturn(actor);

        InviteStaffRequest request = new InviteStaffRequest();
        request.setEmail("staff@test.com");
        request.setPassword("Password123");
        request.setFullName("Staff");
        request.setRole(UserRole.TENANT);

        AppException exception = assertThrows(
                AppException.class,
                () -> landlordStaffService.inviteStaff(
                        ActorContext.builder()
                                .keycloakId("kc-landlord")
                                .requestId("r1")
                                .build(),
                        landlordId,
                        request));

        assertEquals(UserServiceErrorCode.STAFF_ROLE_INVALID, exception.getErrorCode());
    }

    @Test
    void shouldRejectDuplicateBuildingPermissionWhenReplace() {
        UUID landlordId = UUID.randomUUID();
        UUID staffId = UUID.randomUUID();
        UUID buildingId = UUID.randomUUID();

        UserProfile actor = new UserProfile();
        actor.setId(landlordId);
        actor.setRole(UserRole.LANDLORD);
        actor.setIsActive(true);

        UserProfile staff = new UserProfile();
        staff.setId(staffId);
        staff.setRole(UserRole.MANAGER);
        staff.setLandlordId(landlordId);

        when(userProfileAccessService.getRequiredActiveByKeycloakId("kc-landlord"))
                .thenReturn(actor);
        when(userProfileRepository.findByIdAndDeletedAt(staffId, 0L)).thenReturn(java.util.Optional.of(staff));
        when(permissionRepository.findByUserIdAndDeletedAt(staffId, 0L)).thenReturn(List.of());

        StaffBuildingPermissionInput first = new StaffBuildingPermissionInput();
        first.setBuildingId(buildingId);
        first.setPermission(UserPermission.MANAGE_ROOM);

        StaffBuildingPermissionInput second = new StaffBuildingPermissionInput();
        second.setBuildingId(buildingId);
        second.setPermission(UserPermission.MANAGE_BILLING);

        ReplaceStaffPermissionsRequest request = new ReplaceStaffPermissionsRequest();
        request.setPermissions(List.of(first, second));

        AppException exception = assertThrows(
                AppException.class,
                () -> landlordStaffService.replacePermissions(
                        ActorContext.builder()
                                .keycloakId("kc-landlord")
                                .requestId("r1")
                                .build(),
                        landlordId,
                        staffId,
                        request));

        assertEquals(UserServiceErrorCode.STAFF_PERMISSION_DUPLICATE_BUILDING, exception.getErrorCode());
    }
}
