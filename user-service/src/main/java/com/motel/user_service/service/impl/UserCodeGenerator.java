package com.motel.user_service.service.impl;

import static sharing.constant.user_service.UserServiceDomainConstant.USER_CODE_PREFIX;
import static sharing.constant.user_service.UserServiceDomainConstant.USER_CODE_SUFFIX_LENGTH;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UserCodeGenerator {
    public String nextCode() {
        return USER_CODE_PREFIX + UUID.randomUUID().toString().substring(0, USER_CODE_SUFFIX_LENGTH);
    }
}
