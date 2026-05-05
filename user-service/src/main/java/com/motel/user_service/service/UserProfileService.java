package com.motel.user_service.service;

import com.motel.user_service.dto.user_profile.UserProfileRequest;
import com.motel.user_service.dto.user_profile.UserProfileResponse;
import com.motel.user_service.entity.UserProfile;
import java.util.UUID;
import sharing.base.service.BaseService;

public interface UserProfileService extends BaseService<UserProfile, UUID, UserProfileRequest, UserProfileResponse> {}
