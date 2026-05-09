package com.motel.user_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import sharing.constant.user_service.UserServiceHeaderConstants;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.internal-auth")
public class InternalAuthProperties {
    private boolean enabled = false;
    private String token;
    private String headerName = UserServiceHeaderConstants.HEADER_INTERNAL_TOKEN;
}
