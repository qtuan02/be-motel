package com.motel.user_service.mapper;

import com.motel.user_service.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sharing.config.GlobalMapperConfig;
import sharing.dto.user_service.common.UserSummaryResponse;

@Mapper(config = GlobalMapperConfig.class)
public interface UserProfileMapper {
    @Mapping(target = "permissions", ignore = true)
    UserSummaryResponse toSummary(UserProfile entity);
}
