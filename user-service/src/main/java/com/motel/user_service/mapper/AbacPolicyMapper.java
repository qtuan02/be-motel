package com.motel.user_service.mapper;

import com.motel.user_service.entity.AbacPolicy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sharing.config.GlobalMapperConfig;
import sharing.dto.user_service.policy.AbacPolicyResponse;

@Mapper(config = GlobalMapperConfig.class)
public interface AbacPolicyMapper {
    @Mapping(target = "systemPolicy", source = "isSystemPolicy")
    AbacPolicyResponse toResponse(AbacPolicy entity);
}
