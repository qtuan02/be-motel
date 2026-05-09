package com.motel.user_service.auth;

import com.motel.user_service.config.InternalAuthProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import sharing.exception.AppException;
import sharing.exception.user_service.UserServiceErrorCode;

@Component
public class InternalAuthGuard {
    private final InternalAuthProperties properties;

    public InternalAuthGuard(InternalAuthProperties properties) {
        this.properties = properties;
    }

    public void check(HttpServletRequest request) {
        if (!properties.isEnabled()) {
            return;
        }
        String configuredToken = properties.getToken();
        String headerName = properties.getHeaderName();
        String provided = request.getHeader(headerName);
        if (configuredToken == null
                || configuredToken.isBlank()
                || provided == null
                || !configuredToken.equals(provided)) {
            throw new AppException(UserServiceErrorCode.INTERNAL_AUTH_INVALID);
        }
    }
}
