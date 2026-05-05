package com.motel.user_service.mapper;

import com.motel.user_service.dto.user_profile.UserProfileRequest;
import com.motel.user_service.dto.user_profile.UserProfileResponse;
import com.motel.user_service.entity.UserProfile;
import org.mapstruct.Mapper;
import sharing.base.mapper.BaseMapper;
import sharing.config.GlobalMapperConfig;

@Mapper(config = GlobalMapperConfig.class)
public interface UserProfileMapper extends BaseMapper<UserProfile, UserProfileRequest, UserProfileResponse> {}
