package com.motel.user_service.service;

import com.motel.user_service.auth.ActorContext;
import java.util.UUID;
import sharing.dto.user_service.current_user.CurrentUserResponse;
import sharing.dto.user_service.current_user.FcmTokenResponse;
import sharing.dto.user_service.current_user.RegisterFcmTokenRequest;
import sharing.dto.user_service.current_user.UpdateCurrentUserRequest;
import sharing.dto.user_service.current_user.UpdateZaloUidRequest;

public interface CurrentUserService {
    CurrentUserResponse getCurrentUser(ActorContext actorContext);

    CurrentUserResponse updateCurrentUser(ActorContext actorContext, UpdateCurrentUserRequest request);

    FcmTokenResponse registerFcmToken(ActorContext actorContext, RegisterFcmTokenRequest request);

    void revokeFcmToken(ActorContext actorContext, UUID tokenId);

    CurrentUserResponse updateZaloUid(ActorContext actorContext, UpdateZaloUidRequest request);
}
