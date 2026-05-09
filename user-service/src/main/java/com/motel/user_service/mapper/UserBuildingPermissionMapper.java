package com.motel.user_service.mapper;

import com.motel.user_service.entity.UserBuildingPermission;
import org.mapstruct.Mapper;
import sharing.config.GlobalMapperConfig;
import sharing.dto.user_service.common.BuildingPermissionResponse;

@Mapper(config = GlobalMapperConfig.class)
public interface UserBuildingPermissionMapper {
    BuildingPermissionResponse toResponse(UserBuildingPermission entity);
}
