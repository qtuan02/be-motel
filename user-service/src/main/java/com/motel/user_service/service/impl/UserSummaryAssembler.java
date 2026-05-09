package com.motel.user_service.service.impl;

import static sharing.constant.user_service.UserServiceDomainConstant.ACTIVE_DELETED_AT;

import com.motel.user_service.entity.UserBuildingPermission;
import com.motel.user_service.entity.UserProfile;
import com.motel.user_service.mapper.UserBuildingPermissionMapper;
import com.motel.user_service.mapper.UserProfileMapper;
import com.motel.user_service.repository.UserBuildingPermissionRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import sharing.dto.user_service.common.BuildingPermissionResponse;
import sharing.dto.user_service.common.UserSummaryResponse;

@Service
public class UserSummaryAssembler {
    private final UserProfileMapper userProfileMapper;
    private final UserBuildingPermissionMapper permissionMapper;
    private final UserBuildingPermissionRepository permissionRepository;

    public UserSummaryAssembler(
            UserProfileMapper userProfileMapper,
            UserBuildingPermissionMapper permissionMapper,
            UserBuildingPermissionRepository permissionRepository) {
        this.userProfileMapper = userProfileMapper;
        this.permissionMapper = permissionMapper;
        this.permissionRepository = permissionRepository;
    }

    public UserSummaryResponse toSummary(UserProfile profile) {
        UserSummaryResponse response = userProfileMapper.toSummary(profile);
        List<BuildingPermissionResponse> permissions =
                permissionRepository.findByUserIdAndDeletedAt(profile.getId(), ACTIVE_DELETED_AT).stream()
                        .map(permissionMapper::toResponse)
                        .toList();
        response.setPermissions(permissions);
        return response;
    }

    public List<UserSummaryResponse> toSummaryList(Collection<UserProfile> profiles) {
        List<UserProfile> profileList = new ArrayList<>(profiles);
        List<UUID> userIds = profileList.stream().map(UserProfile::getId).toList();
        Map<UUID, List<BuildingPermissionResponse>> permissionMap =
                permissionRepository.findByUserIdInAndDeletedAt(userIds, ACTIVE_DELETED_AT).stream()
                        .collect(Collectors.groupingBy(
                                UserBuildingPermission::getUserId,
                                Collectors.mapping(permissionMapper::toResponse, Collectors.toList())));

        return profileList.stream()
                .map(profile -> {
                    UserSummaryResponse response = userProfileMapper.toSummary(profile);
                    response.setPermissions(permissionMap.getOrDefault(profile.getId(), List.of()));
                    return response;
                })
                .toList();
    }
}
