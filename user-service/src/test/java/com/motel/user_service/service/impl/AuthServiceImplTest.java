package com.motel.user_service.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.motel.user_service.adapter.IdentityProviderClient;
import com.motel.user_service.auth.ActorContext;
import com.motel.user_service.entity.UserProfile;
import com.motel.user_service.repository.UserProfileRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import sharing.dto.user_service.auth.RegisterLandlordRequest;
import sharing.dto.user_service.auth.RegisterLandlordResponse;
import sharing.dto.user_service.common.UserSummaryResponse;
import sharing.enums.user_service.UserRole;
import sharing.exception.AppException;
import sharing.exception.user_service.UserServiceErrorCode;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private IdentityProviderClient identityProviderClient;

    @Mock
    private UserProfilePiiUpdater userProfilePiiUpdater;

    @Mock
    private UserSummaryAssembler userSummaryAssembler;

    @Mock
    private AuthAuditService authAuditService;

    @Mock
    private UserCodeGenerator userCodeGenerator;

    @Mock
    private PlatformTransactionManager transactionManager;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                userProfileRepository,
                identityProviderClient,
                userProfilePiiUpdater,
                userSummaryAssembler,
                authAuditService,
                userCodeGenerator,
                new TransactionTemplate(transactionManager));
    }

    @Test
    void shouldRejectWhenEmailAlreadyExists() {
        RegisterLandlordRequest request = new RegisterLandlordRequest();
        request.setEmail("owner@test.com");
        request.setPassword("Password123");
        request.setFullName("Owner");

        when(userProfileRepository.findByEmailNormalizedAndDeletedAt("owner@test.com", 0L))
                .thenReturn(Optional.of(new UserProfile()));

        AppException exception = assertThrows(
                AppException.class,
                () -> authService.registerLandlord(
                        ActorContext.builder().requestId("r1").build(), request));

        assertEquals(UserServiceErrorCode.USER_EMAIL_ALREADY_EXISTS, exception.getErrorCode());
    }

    @Test
    void shouldCreateLandlordProfileWithoutPersistingPlainPhone() {
        RegisterLandlordRequest request = new RegisterLandlordRequest();
        request.setEmail("owner@test.com");
        request.setPassword("Password123");
        request.setFullName("Owner");
        request.setPhone("0987654321");

        when(userProfileRepository.findByEmailNormalizedAndDeletedAt("owner@test.com", 0L))
                .thenReturn(Optional.empty());
        when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        when(identityProviderClient.createUser("owner@test.com", "Password123", "Owner"))
                .thenReturn("kc-001");
        when(userCodeGenerator.nextCode()).thenReturn("USR-12345678");
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> {
            UserProfile profile = invocation.getArgument(0);
            profile.setId(UUID.randomUUID());
            profile.setRole(UserRole.LANDLORD);
            profile.setPhoneCiphertext("enc-phone");
            return profile;
        });

        UserSummaryResponse summary = new UserSummaryResponse();
        when(userSummaryAssembler.toSummary(any(UserProfile.class))).thenReturn(summary);

        RegisterLandlordResponse response = authService.registerLandlord(
                ActorContext.builder().requestId("request-1").build(), request);

        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository).save(captor.capture());
        UserProfile saved = captor.getValue();

        assertEquals("kc-001", saved.getKeycloakId());
        assertEquals("USR-12345678", saved.getCode());
        assertNotEquals("0987654321", saved.getPhoneCiphertext());
        assertEquals(summary, response.getProfile());
    }
}
