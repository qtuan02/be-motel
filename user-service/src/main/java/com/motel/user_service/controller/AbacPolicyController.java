package com.motel.user_service.controller;

import com.motel.user_service.auth.ActorContext;
import com.motel.user_service.auth.ActorContextResolver;
import com.motel.user_service.service.AbacPolicyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sharing.constant.user_service.UserServiceApiPaths;
import sharing.dto.user_service.policy.AbacPolicyResponse;
import sharing.dto.user_service.policy.CreateAbacPolicyRequest;

@RestController
@RequestMapping(UserServiceApiPaths.ABAC)
public class AbacPolicyController {
    private final ActorContextResolver actorContextResolver;
    private final AbacPolicyService abacPolicyService;

    public AbacPolicyController(ActorContextResolver actorContextResolver, AbacPolicyService abacPolicyService) {
        this.actorContextResolver = actorContextResolver;
        this.abacPolicyService = abacPolicyService;
    }

    @GetMapping("/policies")
    public List<AbacPolicyResponse> listPolicies(HttpServletRequest httpServletRequest) {
        ActorContext actorContext = actorContextResolver.resolveRequired(httpServletRequest);
        return abacPolicyService.listPolicies(actorContext);
    }

    @PostMapping("/policies")
    public AbacPolicyResponse createPolicy(
            @Valid @RequestBody CreateAbacPolicyRequest request, HttpServletRequest httpServletRequest) {
        ActorContext actorContext = actorContextResolver.resolveRequired(httpServletRequest);
        return abacPolicyService.createPolicy(actorContext, request);
    }

    @DeleteMapping("/policies/{id}")
    public void deletePolicy(@PathVariable("id") UUID id, HttpServletRequest httpServletRequest) {
        ActorContext actorContext = actorContextResolver.resolveRequired(httpServletRequest);
        abacPolicyService.deletePolicy(actorContext, id);
    }
}
