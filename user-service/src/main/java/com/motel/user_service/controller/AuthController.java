package com.motel.user_service.controller;

import com.motel.user_service.auth.ActorContext;
import com.motel.user_service.auth.ActorContextResolver;
import com.motel.user_service.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sharing.constant.user_service.UserServiceApiPaths;
import sharing.dto.user_service.auth.RegisterLandlordRequest;
import sharing.dto.user_service.auth.RegisterLandlordResponse;

@RestController
@RequestMapping(UserServiceApiPaths.AUTH)
public class AuthController {
    private final AuthService authService;
    private final ActorContextResolver actorContextResolver;

    public AuthController(AuthService authService, ActorContextResolver actorContextResolver) {
        this.authService = authService;
        this.actorContextResolver = actorContextResolver;
    }

    @PostMapping("/register")
    public RegisterLandlordResponse register(
            @Valid @RequestBody RegisterLandlordRequest request, HttpServletRequest httpServletRequest) {
        ActorContext actorContext = actorContextResolver
                .resolveOptional(httpServletRequest)
                .orElseGet(() -> ActorContext.builder()
                        .requestId(UUID.randomUUID().toString())
                        .build());
        return authService.registerLandlord(actorContext, request);
    }
}
