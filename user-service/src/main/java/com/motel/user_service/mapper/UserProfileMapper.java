package com.motel.user_service.mapper;

import com.motel.user_service.entity.UserProfile;
import org.mapstruct.Mapper;
import sharing.base.mapper.BaseMapper;
import sharing.configs.GlobalMapperConfig;
import sharing.dtos.user_service.UserProfileRequest;
import sharing.dtos.user_service.UserProfileResponse;

@Mapper(config = GlobalMapperConfig.class)
public interface UserProfileMapper extends BaseMapper<UserProfile, UserProfileRequest, UserProfileResponse> {}
