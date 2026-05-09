package com.motel.user_service.service.impl;

import static sharing.constant.user_service.UserServiceDomainConstant.ACTIVE_DELETED_AT;

import com.motel.user_service.entity.UserProfile;
import com.motel.user_service.repository.UserProfileRepository;
import com.motel.user_service.service.UserProfileAccessService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sharing.exception.AppException;
import sharing.exception.user_service.UserServiceErrorCode;

@Service
public class UserProfileAccessServiceImpl implements UserProfileAccessService {
    private final UserProfileRepository userProfileRepository;

    public UserProfileAccessServiceImpl(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfile getRequiredActiveByKeycloakId(String keycloakId) {
        UserProfile profile = userProfileRepository
                .findByKeycloakIdAndDeletedAt(keycloakId, ACTIVE_DELETED_AT)
                .orElseThrow(() -> new AppException(UserServiceErrorCode.USER_PROFILE_NOT_FOUND));
        if (!Boolean.TRUE.equals(profile.getIsActive())) {
            throw new AppException(UserServiceErrorCode.USER_INACTIVE);
        }
        return profile;
    }
}
