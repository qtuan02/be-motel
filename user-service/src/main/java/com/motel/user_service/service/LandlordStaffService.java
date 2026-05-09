package com.motel.user_service.service;

import com.motel.user_service.auth.ActorContext;
import java.util.List;
import java.util.UUID;
import sharing.dto.user_service.landlord_staff.InviteStaffRequest;
import sharing.dto.user_service.landlord_staff.ReplaceStaffPermissionsRequest;
import sharing.dto.user_service.landlord_staff.StaffResponse;

public interface LandlordStaffService {
    List<StaffResponse> listStaff(ActorContext actorContext, UUID landlordId);

    StaffResponse inviteStaff(ActorContext actorContext, UUID landlordId, InviteStaffRequest request);

    StaffResponse replacePermissions(
            ActorContext actorContext, UUID landlordId, UUID staffId, ReplaceStaffPermissionsRequest request);

    void deactivateStaff(ActorContext actorContext, UUID landlordId, UUID staffId);
}
