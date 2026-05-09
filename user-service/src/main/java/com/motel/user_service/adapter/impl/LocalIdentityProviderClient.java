package com.motel.user_service.adapter.impl;

import com.motel.user_service.adapter.IdentityProviderClient;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class LocalIdentityProviderClient implements IdentityProviderClient {
    @Override
    public String createUser(String email, String rawPassword, String fullName) {
        return "local-" + UUID.randomUUID();
    }

    @Override
    public void disableUser(String externalUserId) {
        // No-op local implementation for bounded phase.
    }
}
