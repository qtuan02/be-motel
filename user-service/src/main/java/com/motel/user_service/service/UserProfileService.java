package com.motel.user_service.service;

import com.motel.user_service.entity.UserProfile;
import java.util.UUID;
import sharing.base.service.BaseService;
import sharing.dtos.user_service.UserProfileRequest;
import sharing.dtos.user_service.UserProfileResponse;

public interface UserProfileService extends BaseService<UserProfile, UUID, UserProfileRequest, UserProfileResponse> {}
