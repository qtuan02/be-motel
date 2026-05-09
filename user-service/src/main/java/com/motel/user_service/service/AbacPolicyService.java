package com.motel.user_service.service;

import com.motel.user_service.auth.ActorContext;
import java.util.List;
import java.util.UUID;
import sharing.dto.user_service.policy.AbacPolicyResponse;
import sharing.dto.user_service.policy.CreateAbacPolicyRequest;

public interface AbacPolicyService {
    List<AbacPolicyResponse> listPolicies(ActorContext actorContext);

    AbacPolicyResponse createPolicy(ActorContext actorContext, CreateAbacPolicyRequest request);

    void deletePolicy(ActorContext actorContext, UUID policyId);
}
