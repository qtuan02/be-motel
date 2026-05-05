package com.motel.user_service.service.impl;

import com.motel.user_service.dto.user_profile.UserProfileRequest;
import com.motel.user_service.dto.user_profile.UserProfileResponse;
import com.motel.user_service.entity.UserProfile;
import com.motel.user_service.mapper.UserProfileMapper;
import com.motel.user_service.repository.UserProfileRepository;
import com.motel.user_service.service.UserProfileService;
import java.util.UUID;
import org.springframework.stereotype.Service;
import sharing.base.service.impl.BaseServiceImpl;

@Service
public class UserProfileServiceImpl
        extends BaseServiceImpl<UserProfile, UUID, UserProfileRequest, UserProfileResponse, UserProfileRepository>
        implements UserProfileService {

    public UserProfileServiceImpl(UserProfileRepository repository, UserProfileMapper mapper) {
        super(repository, mapper, UserProfile.class);
    }
}
