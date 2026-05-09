package com.motel.user_service.controller;

import com.motel.user_service.auth.ActorContext;
import com.motel.user_service.auth.InternalAuthGuard;
import com.motel.user_service.service.InternalClaimsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sharing.constant.user_service.UserServiceApiPaths;
import sharing.constant.user_service.UserServiceHeaderConstants;
import sharing.dto.user_service.internal.InternalClaimsResponse;

@RestController
@RequestMapping(UserServiceApiPaths.INTERNAL_USERS)
public class InternalClaimsController {
    private final InternalClaimsService internalClaimsService;
    private final InternalAuthGuard internalAuthGuard;

    public InternalClaimsController(InternalClaimsService internalClaimsService, InternalAuthGuard internalAuthGuard) {
        this.internalClaimsService = internalClaimsService;
        this.internalAuthGuard = internalAuthGuard;
    }

    @GetMapping("/{keycloakId}/claims")
    public InternalClaimsResponse getClaims(
            @PathVariable("keycloakId") String keycloakId, HttpServletRequest httpServletRequest) {
        internalAuthGuard.check(httpServletRequest);
        String requestId = ActorContext.ensureRequestId(
                httpServletRequest.getHeader(UserServiceHeaderConstants.HEADER_REQUEST_ID));
        return internalClaimsService.getClaimsByKeycloakId(keycloakId, requestId);
    }
}
