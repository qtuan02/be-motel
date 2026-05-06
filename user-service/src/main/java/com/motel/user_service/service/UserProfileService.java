package com.motel.user_service.service;

import com.motel.user_service.entity.UserProfile;
import java.util.UUID;
import sharing.base.service.BaseService;
import sharing.dto.user_service.UserProfileRequest;
import sharing.dto.user_service.UserProfileResponse;

public interface UserProfileService extends BaseService<UserProfile, UUID, UserProfileRequest, UserProfileResponse> {}
