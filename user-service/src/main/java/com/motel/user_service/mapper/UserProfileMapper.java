package com.motel.user_service.mapper;

import com.motel.user_service.entity.UserProfile;
import org.mapstruct.Mapper;
import sharing.base.mapper.BaseMapper;
import sharing.config.GlobalMapperConfig;
import sharing.dto.user_service.UserProfileRequest;
import sharing.dto.user_service.UserProfileResponse;

@Mapper(config = GlobalMapperConfig.class)
public interface UserProfileMapper extends BaseMapper<UserProfile, UserProfileRequest, UserProfileResponse> {}
