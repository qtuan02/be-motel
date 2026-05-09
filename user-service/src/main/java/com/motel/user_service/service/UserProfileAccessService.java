package com.motel.user_service.service;

import com.motel.user_service.entity.UserProfile;

public interface UserProfileAccessService {
    UserProfile getRequiredActiveByKeycloakId(String keycloakId);
}
