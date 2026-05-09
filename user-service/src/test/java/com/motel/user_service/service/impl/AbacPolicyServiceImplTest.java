package com.motel.user_service.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.motel.user_service.auth.ActorContext;
import com.motel.user_service.entity.AbacPolicy;
import com.motel.user_service.entity.UserProfile;
import com.motel.user_service.mapper.AbacPolicyMapper;
import com.motel.user_service.repository.AbacPolicyRepository;
import com.motel.user_service.service.UserProfileAccessService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sharing.enums.user_service.UserRole;
import sharing.exception.AppException;
import sharing.exception.user_service.UserServiceErrorCode;

@ExtendWith(MockitoExtension.class)
class AbacPolicyServiceImplTest {

    @Mock
    private AbacPolicyRepository abacPolicyRepository;

    @Mock
    private UserProfileAccessService userProfileAccessService;

    @Mock
    private AbacPolicyMapper abacPolicyMapper;

    @Mock
    private AuthAuditService authAuditService;

    private AbacPolicyServiceImpl abacPolicyService;

    @BeforeEach
    void setUp() {
        abacPolicyService = new AbacPolicyServiceImpl(
                abacPolicyRepository, userProfileAccessService, abacPolicyMapper, new ObjectMapper(), authAuditService);
    }

    @Test
    void shouldRejectDeleteSystemPolicy() {
        UUID landlordId = UUID.randomUUID();
        UUID policyId = UUID.randomUUID();

        UserProfile actor = new UserProfile();
        actor.setId(landlordId);
        actor.setRole(UserRole.LANDLORD);
        actor.setIsActive(true);

        AbacPolicy policy = new AbacPolicy();
        policy.setId(policyId);
        policy.setIsSystemPolicy(true);

        when(userProfileAccessService.getRequiredActiveByKeycloakId("kc-landlord"))
                .thenReturn(actor);
        when(abacPolicyRepository.findByIdAndDeletedAt(policyId, 0L)).thenReturn(Optional.of(policy));

        AppException exception = assertThrows(
                AppException.class,
                () -> abacPolicyService.deletePolicy(
                        ActorContext.builder()
                                .keycloakId("kc-landlord")
                                .requestId("r1")
                                .build(),
                        policyId));

        assertEquals(UserServiceErrorCode.ABAC_POLICY_SYSTEM_READ_ONLY, exception.getErrorCode());
    }
}
