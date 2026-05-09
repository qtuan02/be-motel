package com.motel.user_service.service;

import com.motel.user_service.auth.ActorContext;
import sharing.dto.user_service.auth.RegisterLandlordRequest;
import sharing.dto.user_service.auth.RegisterLandlordResponse;

public interface AuthService {
    RegisterLandlordResponse registerLandlord(ActorContext actorContext, RegisterLandlordRequest request);
}
