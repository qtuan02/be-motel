package com.motel.user_service.controller;

import com.motel.user_service.entity.UserProfile;
import com.motel.user_service.service.UserProfileService;
import java.util.UUID;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sharing.base.controller.BaseController;
import sharing.constant.UserSerivceConstant;
import sharing.dto.user_service.UserProfileRequest;
import sharing.dto.user_service.UserProfileResponse;

@RestController
@RequestMapping(UserSerivceConstant.USER_PROFILES_API)
public class UserProfileController
        extends BaseController<UserProfile, UUID, UserProfileRequest, UserProfileResponse, UserProfileService> {

    public UserProfileController(UserProfileService service) {
        super(service);
    }
}
