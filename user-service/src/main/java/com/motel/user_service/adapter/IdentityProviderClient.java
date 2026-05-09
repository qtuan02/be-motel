package com.motel.user_service.adapter;

public interface IdentityProviderClient {
    String createUser(String email, String rawPassword, String fullName);

    void disableUser(String externalUserId);
}
