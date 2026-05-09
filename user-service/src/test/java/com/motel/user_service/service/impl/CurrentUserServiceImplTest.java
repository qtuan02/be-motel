package com.motel.user_service.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.motel.user_service.adapter.PiiCryptoService;
import com.motel.user_service.auth.ActorContext;
import com.motel.user_service.entity.FcmToken;
import com.motel.user_service.entity.UserProfile;
import com.motel.user_service.mapper.FcmTokenMapper;
import com.motel.user_service.repository.FcmTokenRepository;
import com.motel.user_service.repository.UserProfileRepository;
import com.motel.user_service.service.UserProfileAccessService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sharing.dto.user_service.current_user.FcmTokenResponse;
import sharing.dto.user_service.current_user.RegisterFcmTokenRequest;
import sharing.enums.user_service.DeviceType;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceImplTest {

    @Mock
    private UserProfileAccessService userProfileAccessService;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserSummaryAssembler userSummaryAssembler;

    @Mock
    private PiiCryptoService piiCryptoService;

    @Mock
    private UserProfilePiiUpdater userProfilePiiUpdater;

    @Mock
    private FcmTokenRepository fcmTokenRepository;

    @Mock
    private FcmTokenMapper fcmTokenMapper;

    @Mock
    private AuthAuditService authAuditService;

    private CurrentUserServiceImpl currentUserService;

    @BeforeEach
    void setUp() {
        currentUserService = new CurrentUserServiceImpl(
                userProfileAccessService,
                userProfileRepository,
                userSummaryAssembler,
                piiCryptoService,
                userProfilePiiUpdater,
                fcmTokenRepository,
                fcmTokenMapper,
                authAuditService);
    }

    @Test
    void shouldRegisterFcmTokenIdempotentlyByTokenHash() {
        UUID userId = UUID.randomUUID();
        UUID tokenId = UUID.randomUUID();

        UserProfile profile = new UserProfile();
        profile.setId(userId);
        profile.setIsActive(true);

        FcmToken existingToken = new FcmToken();
        existingToken.setId(tokenId);
        existingToken.setUserId(userId);
        existingToken.setIsActive(false);
        existingToken.setDeletedAt(0L);

        RegisterFcmTokenRequest request = new RegisterFcmTokenRequest();
        request.setToken("token-value");
        request.setDeviceType(DeviceType.ANDROID);

        when(userProfileAccessService.getRequiredActiveByKeycloakId("kc-user")).thenReturn(profile);
        when(piiCryptoService.hash("token-value")).thenReturn("token-hash");
        when(piiCryptoService.encrypt("token-value")).thenReturn("token-enc");
        when(fcmTokenRepository.findByUserIdAndTokenHashAndDeletedAt(userId, "token-hash", 0L))
                .thenReturn(Optional.of(existingToken));
        when(fcmTokenRepository.save(any(FcmToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FcmTokenResponse mapped = new FcmTokenResponse();
        mapped.setId(tokenId);
        mapped.setDeviceType(DeviceType.ANDROID);
        mapped.setIsActive(true);
        when(fcmTokenMapper.toResponse(any(FcmToken.class))).thenReturn(mapped);

        FcmTokenResponse response = currentUserService.registerFcmToken(
                ActorContext.builder().keycloakId("kc-user").requestId("r1").build(), request);

        assertEquals(tokenId, response.getId());
        assertEquals(true, response.getIsActive());
    }
}
