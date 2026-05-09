package com.motel.user_service.service.impl;

import static sharing.constant.user_service.UserServiceDomainConstant.ACTIVE_DELETED_AT;

import com.motel.user_service.adapter.IdentityProviderClient;
import com.motel.user_service.auth.ActorContext;
import com.motel.user_service.entity.UserProfile;
import com.motel.user_service.repository.UserProfileRepository;
import com.motel.user_service.service.AuthService;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import sharing.dto.user_service.auth.RegisterLandlordRequest;
import sharing.dto.user_service.auth.RegisterLandlordResponse;
import sharing.enums.user_service.AuditEventType;
import sharing.enums.user_service.AuditResult;
import sharing.enums.user_service.UserRole;
import sharing.exception.AppException;
import sharing.exception.user_service.UserServiceErrorCode;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserProfileRepository userProfileRepository;
    private final IdentityProviderClient identityProviderClient;
    private final UserProfilePiiUpdater userProfilePiiUpdater;
    private final UserSummaryAssembler userSummaryAssembler;
    private final AuthAuditService authAuditService;
    private final UserCodeGenerator userCodeGenerator;
    private final TransactionTemplate transactionTemplate;

    public AuthServiceImpl(
            UserProfileRepository userProfileRepository,
            IdentityProviderClient identityProviderClient,
            UserProfilePiiUpdater userProfilePiiUpdater,
            UserSummaryAssembler userSummaryAssembler,
            AuthAuditService authAuditService,
            UserCodeGenerator userCodeGenerator,
            TransactionTemplate transactionTemplate) {
        this.userProfileRepository = userProfileRepository;
        this.identityProviderClient = identityProviderClient;
        this.userProfilePiiUpdater = userProfilePiiUpdater;
        this.userSummaryAssembler = userSummaryAssembler;
        this.authAuditService = authAuditService;
        this.userCodeGenerator = userCodeGenerator;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public RegisterLandlordResponse registerLandlord(ActorContext actorContext, RegisterLandlordRequest request) {
        String emailNormalized = normalizeEmail(request.getEmail());
        if (userProfileRepository
                .findByEmailNormalizedAndDeletedAt(emailNormalized, ACTIVE_DELETED_AT)
                .isPresent()) {
            throw new AppException(UserServiceErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }

        String externalId =
                identityProviderClient.createUser(request.getEmail(), request.getPassword(), request.getFullName());
        String requestId = resolveRequestId(actorContext);

        UserProfile saved = transactionTemplate.execute(status -> {
            UserProfile profile = new UserProfile();
            profile.setCode(userCodeGenerator.nextCode());
            profile.setKeycloakId(externalId);
            profile.setRole(UserRole.LANDLORD);
            profile.setFullName(request.getFullName().trim());
            profile.setEmail(request.getEmail().trim());
            profile.setEmailNormalized(emailNormalized);
            profile.setAvatarUrl(request.getAvatarUrl());
            userProfilePiiUpdater.applyPhone(profile, request.getPhone());

            UserProfile created = userProfileRepository.save(profile);
            authAuditService.log(
                    requestId,
                    created.getId(),
                    created.getId(),
                    AuditEventType.LANDLORD_REGISTERED,
                    AuditResult.SUCCESS,
                    Map.of("role", created.getRole().name()));
            return created;
        });
        if (saved == null) {
            throw new IllegalStateException("Failed to persist landlord profile");
        }

        RegisterLandlordResponse response = new RegisterLandlordResponse();
        response.setProfile(userSummaryAssembler.toSummary(saved));
        response.setClaims(List.of("sub", "role", "landlord_id", "sensitivity_clearance"));
        return response;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String resolveRequestId(ActorContext actorContext) {
        if (actorContext == null) {
            return UUID.randomUUID().toString();
        }
        return ActorContext.ensureRequestId(actorContext.getRequestId());
    }
}
