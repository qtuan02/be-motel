package com.motel.user_service.auth;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.stereotype.Component;
import sharing.constant.user_service.UserServiceHeaderConstants;
import sharing.exception.AppException;
import sharing.exception.user_service.UserServiceErrorCode;

@Component
public class ActorContextResolver {

    public ActorContext resolveRequired(HttpServletRequest request) {
        String keycloakId = request.getHeader(UserServiceHeaderConstants.HEADER_ACTOR_KEYCLOAK_ID);
        if (keycloakId == null || keycloakId.isBlank()) {
            throw new AppException(UserServiceErrorCode.ACTOR_CONTEXT_MISSING);
        }
        return ActorContext.builder()
                .keycloakId(keycloakId.trim())
                .requestId(
                        ActorContext.ensureRequestId(request.getHeader(UserServiceHeaderConstants.HEADER_REQUEST_ID)))
                .build();
    }

    public Optional<ActorContext> resolveOptional(HttpServletRequest request) {
        String keycloakId = request.getHeader(UserServiceHeaderConstants.HEADER_ACTOR_KEYCLOAK_ID);
        if (keycloakId == null || keycloakId.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(ActorContext.builder()
                .keycloakId(keycloakId.trim())
                .requestId(
                        ActorContext.ensureRequestId(request.getHeader(UserServiceHeaderConstants.HEADER_REQUEST_ID)))
                .build());
    }
}
