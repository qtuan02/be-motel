package com.motel.user_service.service.impl;

import static sharing.constant.user_service.UserServiceDomainConstant.ACTIVE_DELETED_AT;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.motel.user_service.auth.ActorContext;
import com.motel.user_service.entity.AbacPolicy;
import com.motel.user_service.entity.UserProfile;
import com.motel.user_service.mapper.AbacPolicyMapper;
import com.motel.user_service.repository.AbacPolicyRepository;
import com.motel.user_service.service.AbacPolicyService;
import com.motel.user_service.service.UserProfileAccessService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sharing.dto.user_service.policy.AbacPolicyResponse;
import sharing.dto.user_service.policy.CreateAbacPolicyRequest;
import sharing.enums.user_service.AuditEventType;
import sharing.enums.user_service.AuditResult;
import sharing.enums.user_service.UserRole;
import sharing.exception.AppException;
import sharing.exception.user_service.UserServiceErrorCode;

@Service
public class AbacPolicyServiceImpl implements AbacPolicyService {
    private final AbacPolicyRepository abacPolicyRepository;
    private final UserProfileAccessService userProfileAccessService;
    private final AbacPolicyMapper abacPolicyMapper;
    private final ObjectMapper objectMapper;
    private final AuthAuditService authAuditService;

    public AbacPolicyServiceImpl(
            AbacPolicyRepository abacPolicyRepository,
            UserProfileAccessService userProfileAccessService,
            AbacPolicyMapper abacPolicyMapper,
            ObjectMapper objectMapper,
            AuthAuditService authAuditService) {
        this.abacPolicyRepository = abacPolicyRepository;
        this.userProfileAccessService = userProfileAccessService;
        this.abacPolicyMapper = abacPolicyMapper;
        this.objectMapper = objectMapper;
        this.authAuditService = authAuditService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AbacPolicyResponse> listPolicies(ActorContext actorContext) {
        UserProfile actor = requireLandlord(actorContext);
        return abacPolicyRepository.findVisiblePolicies(actor.getId()).stream()
                .map(abacPolicyMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AbacPolicyResponse createPolicy(ActorContext actorContext, CreateAbacPolicyRequest request) {
        UserProfile actor = requireLandlord(actorContext);
        validateConditions(request.getConditions());

        AbacPolicy policy = new AbacPolicy();
        policy.setLandlordId(actor.getId());
        policy.setName(request.getName().trim());
        policy.setResource(request.getResource().trim());
        policy.setAction(request.getAction().trim());
        policy.setPolicyEffect(request.getPolicyEffect());
        policy.setConditions(request.getConditions().trim());
        policy.setIsSystemPolicy(false);

        AbacPolicy saved = abacPolicyRepository.save(policy);
        authAuditService.log(
                actorContext.getRequestId(),
                actor.getId(),
                actor.getId(),
                AuditEventType.ABAC_POLICY_CREATED,
                AuditResult.SUCCESS,
                Map.of("policyId", saved.getId()));
        return abacPolicyMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deletePolicy(ActorContext actorContext, UUID policyId) {
        UserProfile actor = requireLandlord(actorContext);
        AbacPolicy policy = abacPolicyRepository
                .findByIdAndDeletedAt(policyId, ACTIVE_DELETED_AT)
                .orElseThrow(() -> new AppException(UserServiceErrorCode.ABAC_POLICY_NOT_FOUND));

        if (Boolean.TRUE.equals(policy.getIsSystemPolicy())) {
            throw new AppException(UserServiceErrorCode.ABAC_POLICY_SYSTEM_READ_ONLY);
        }
        if (!actor.getId().equals(policy.getLandlordId())) {
            throw new AppException(UserServiceErrorCode.LANDLORD_ACCESS_DENIED);
        }

        policy.setDeletedAt(System.currentTimeMillis());
        abacPolicyRepository.save(policy);
        authAuditService.log(
                actorContext.getRequestId(),
                actor.getId(),
                actor.getId(),
                AuditEventType.ABAC_POLICY_DELETED,
                AuditResult.SUCCESS,
                Map.of("policyId", policyId));
    }

    private UserProfile requireLandlord(ActorContext actorContext) {
        UserProfile actor = userProfileAccessService.getRequiredActiveByKeycloakId(actorContext.getKeycloakId());
        if (actor.getRole() != UserRole.LANDLORD) {
            throw new AppException(UserServiceErrorCode.LANDLORD_ACCESS_DENIED);
        }
        return actor;
    }

    private void validateConditions(String conditions) {
        try {
            objectMapper.readTree(conditions);
        } catch (IOException ex) {
            throw new AppException(UserServiceErrorCode.ABAC_POLICY_CONDITIONS_INVALID);
        }
    }
}
