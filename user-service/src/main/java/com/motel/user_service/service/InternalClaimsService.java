package com.motel.user_service.service;

import sharing.dto.user_service.internal.InternalClaimsResponse;

public interface InternalClaimsService {
    InternalClaimsResponse getClaimsByKeycloakId(String keycloakId, String requestId);
}
