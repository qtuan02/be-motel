package com.motel.user_service.service.impl;

import static sharing.constant.user_service.UserServiceDomainConstant.ACTIVE_DELETED_AT;

import com.motel.user_service.adapter.PiiCryptoService;
import com.motel.user_service.auth.ActorContext;
import com.motel.user_service.entity.FcmToken;
import com.motel.user_service.entity.UserProfile;
import com.motel.user_service.mapper.FcmTokenMapper;
import com.motel.user_service.repository.FcmTokenRepository;
import com.motel.user_service.repository.UserProfileRepository;
import com.motel.user_service.service.CurrentUserService;
import com.motel.user_service.service.UserProfileAccessService;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sharing.dto.user_service.current_user.CurrentUserResponse;
import sharing.dto.user_service.current_user.FcmTokenResponse;
import sharing.dto.user_service.current_user.RegisterFcmTokenRequest;
import sharing.dto.user_service.current_user.UpdateCurrentUserRequest;
import sharing.dto.user_service.current_user.UpdateZaloUidRequest;
import sharing.enums.user_service.AuditEventType;
import sharing.enums.user_service.AuditResult;
import sharing.exception.AppException;
import sharing.exception.user_service.UserServiceErrorCode;

@Service
public class CurrentUserServiceImpl implements CurrentUserService {
    private final UserProfileAccessService userProfileAccessService;
    private final UserProfileRepository userProfileRepository;
    private final UserSummaryAssembler userSummaryAssembler;
    private final PiiCryptoService piiCryptoService;
    private final UserProfilePiiUpdater userProfilePiiUpdater;
    private final FcmTokenRepository fcmTokenRepository;
    private final FcmTokenMapper fcmTokenMapper;
    private final AuthAuditService authAuditService;

    public CurrentUserServiceImpl(
            UserProfileAccessService userProfileAccessService,
            UserProfileRepository userProfileRepository,
            UserSummaryAssembler userSummaryAssembler,
            PiiCryptoService piiCryptoService,
            UserProfilePiiUpdater userProfilePiiUpdater,
            FcmTokenRepository fcmTokenRepository,
            FcmTokenMapper fcmTokenMapper,
            AuthAuditService authAuditService) {
        this.userProfileAccessService = userProfileAccessService;
        this.userProfileRepository = userProfileRepository;
        this.userSummaryAssembler = userSummaryAssembler;
        this.piiCryptoService = piiCryptoService;
        this.userProfilePiiUpdater = userProfilePiiUpdater;
        this.fcmTokenRepository = fcmTokenRepository;
        this.fcmTokenMapper = fcmTokenMapper;
        this.authAuditService = authAuditService;
    }

    @Override
    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser(ActorContext actorContext) {
        UserProfile profile = userProfileAccessService.getRequiredActiveByKeycloakId(actorContext.getKeycloakId());
        return toCurrentUserResponse(profile);
    }

    @Override
    @Transactional
    public CurrentUserResponse updateCurrentUser(ActorContext actorContext, UpdateCurrentUserRequest request) {
        UserProfile profile = userProfileAccessService.getRequiredActiveByKeycloakId(actorContext.getKeycloakId());

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            profile.setFullName(request.getFullName().trim());
        }
        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getPhone() != null) {
            userProfilePiiUpdater.applyPhone(profile, request.getPhone());
        }
        if (request.getZaloUid() != null) {
            userProfilePiiUpdater.applyZaloUid(profile, request.getZaloUid());
        }

        UserProfile saved = userProfileRepository.save(profile);
        authAuditService.log(
                actorContext.getRequestId(),
                saved.getId(),
                saved.getId(),
                AuditEventType.PROFILE_UPDATED,
                AuditResult.SUCCESS,
                Map.of("hasPhone", saved.getPhoneCiphertext() != null));
        return toCurrentUserResponse(saved);
    }

    @Override
    @Transactional
    public FcmTokenResponse registerFcmToken(ActorContext actorContext, RegisterFcmTokenRequest request) {
        UserProfile profile = userProfileAccessService.getRequiredActiveByKeycloakId(actorContext.getKeycloakId());
        String tokenHash = piiCryptoService.hash(request.getToken());

        FcmToken token = fcmTokenRepository
                .findByUserIdAndTokenHashAndDeletedAt(profile.getId(), tokenHash, ACTIVE_DELETED_AT)
                .orElseGet(FcmToken::new);
        token.setUserId(profile.getId());
        token.setTokenHash(tokenHash);
        token.setTokenCiphertext(piiCryptoService.encrypt(request.getToken()));
        token.setDeviceType(request.getDeviceType());
        token.setIsActive(true);
        token.setDeletedAt(ACTIVE_DELETED_AT);

        FcmToken saved = fcmTokenRepository.save(token);
        authAuditService.log(
                actorContext.getRequestId(),
                profile.getId(),
                profile.getId(),
                AuditEventType.FCM_TOKEN_REGISTERED,
                AuditResult.SUCCESS,
                Map.of("tokenId", saved.getId()));
        return fcmTokenMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void revokeFcmToken(ActorContext actorContext, UUID tokenId) {
        UserProfile profile = userProfileAccessService.getRequiredActiveByKeycloakId(actorContext.getKeycloakId());
        FcmToken token = fcmTokenRepository
                .findByIdAndUserIdAndDeletedAt(tokenId, profile.getId(), ACTIVE_DELETED_AT)
                .orElseThrow(() -> new AppException(UserServiceErrorCode.FCM_TOKEN_NOT_FOUND));
        token.setIsActive(false);
        token.setDeletedAt(System.currentTimeMillis());
        fcmTokenRepository.save(token);

        authAuditService.log(
                actorContext.getRequestId(),
                profile.getId(),
                profile.getId(),
                AuditEventType.FCM_TOKEN_REVOKED,
                AuditResult.SUCCESS,
                Map.of("tokenId", tokenId));
    }

    @Override
    @Transactional
    public CurrentUserResponse updateZaloUid(ActorContext actorContext, UpdateZaloUidRequest request) {
        UserProfile profile = userProfileAccessService.getRequiredActiveByKeycloakId(actorContext.getKeycloakId());
        userProfilePiiUpdater.applyZaloUid(profile, request.getZaloUid());

        UserProfile saved = userProfileRepository.save(profile);
        authAuditService.log(
                actorContext.getRequestId(),
                saved.getId(),
                saved.getId(),
                AuditEventType.ZALO_UID_UPDATED,
                AuditResult.SUCCESS,
                Map.of());
        return toCurrentUserResponse(saved);
    }

    private CurrentUserResponse toCurrentUserResponse(UserProfile profile) {
        CurrentUserResponse response = new CurrentUserResponse();
        response.setProfile(userSummaryAssembler.toSummary(profile));
        response.setZaloLinked(profile.getZaloUidCiphertext() != null
                && !profile.getZaloUidCiphertext().isBlank());
        return response;
    }
}
