package com.motel.user_service.controller;

import com.motel.user_service.auth.ActorContext;
import com.motel.user_service.auth.ActorContextResolver;
import com.motel.user_service.service.CurrentUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sharing.constant.user_service.UserServiceApiPaths;
import sharing.dto.user_service.current_user.CurrentUserResponse;
import sharing.dto.user_service.current_user.FcmTokenResponse;
import sharing.dto.user_service.current_user.RegisterFcmTokenRequest;
import sharing.dto.user_service.current_user.UpdateCurrentUserRequest;
import sharing.dto.user_service.current_user.UpdateZaloUidRequest;

@RestController
@RequestMapping(UserServiceApiPaths.USERS_ME)
public class CurrentUserController {
    private final ActorContextResolver actorContextResolver;
    private final CurrentUserService currentUserService;

    public CurrentUserController(ActorContextResolver actorContextResolver, CurrentUserService currentUserService) {
        this.actorContextResolver = actorContextResolver;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public CurrentUserResponse me(HttpServletRequest httpServletRequest) {
        ActorContext actorContext = actorContextResolver.resolveRequired(httpServletRequest);
        return currentUserService.getCurrentUser(actorContext);
    }

    @PutMapping
    public CurrentUserResponse updateMe(
            @Valid @RequestBody UpdateCurrentUserRequest request, HttpServletRequest httpServletRequest) {
        ActorContext actorContext = actorContextResolver.resolveRequired(httpServletRequest);
        return currentUserService.updateCurrentUser(actorContext, request);
    }

    @PostMapping("/fcm-token")
    public FcmTokenResponse registerFcmToken(
            @Valid @RequestBody RegisterFcmTokenRequest request, HttpServletRequest httpServletRequest) {
        ActorContext actorContext = actorContextResolver.resolveRequired(httpServletRequest);
        return currentUserService.registerFcmToken(actorContext, request);
    }

    @DeleteMapping("/fcm-token/{id}")
    public void revokeFcmToken(@PathVariable("id") UUID id, HttpServletRequest httpServletRequest) {
        ActorContext actorContext = actorContextResolver.resolveRequired(httpServletRequest);
        currentUserService.revokeFcmToken(actorContext, id);
    }

    @PutMapping("/zalo-uid")
    public CurrentUserResponse updateZaloUid(
            @Valid @RequestBody UpdateZaloUidRequest request, HttpServletRequest httpServletRequest) {
        ActorContext actorContext = actorContextResolver.resolveRequired(httpServletRequest);
        return currentUserService.updateZaloUid(actorContext, request);
    }
}
