package com.motel.user_service.auth;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ActorContext {
    String keycloakId;
    String requestId;

    public static String ensureRequestId(String requestId) {
        if (requestId != null && !requestId.isBlank()) {
            return requestId;
        }
        return UUID.randomUUID().toString();
    }
}
