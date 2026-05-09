package com.motel.user_service.mapper;

import com.motel.user_service.entity.FcmToken;
import org.mapstruct.Mapper;
import sharing.config.GlobalMapperConfig;
import sharing.dto.user_service.current_user.FcmTokenResponse;

@Mapper(config = GlobalMapperConfig.class)
public interface FcmTokenMapper {
    FcmTokenResponse toResponse(FcmToken entity);
}
