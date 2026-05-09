package com.motel.user_service.controller;

import com.motel.user_service.auth.ActorContext;
import com.motel.user_service.auth.ActorContextResolver;
import com.motel.user_service.service.LandlordStaffService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
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
import sharing.dto.user_service.landlord_staff.InviteStaffRequest;
import sharing.dto.user_service.landlord_staff.ReplaceStaffPermissionsRequest;
import sharing.dto.user_service.landlord_staff.StaffResponse;

@RestController
@RequestMapping(UserServiceApiPaths.LANDLORDS)
public class LandlordStaffController {
    private final ActorContextResolver actorContextResolver;
    private final LandlordStaffService landlordStaffService;

    public LandlordStaffController(
            ActorContextResolver actorContextResolver, LandlordStaffService landlordStaffService) {
        this.actorContextResolver = actorContextResolver;
        this.landlordStaffService = landlordStaffService;
    }

    @GetMapping("/{landlordId}/staff")
    public List<StaffResponse> listStaff(
            @PathVariable("landlordId") UUID landlordId, HttpServletRequest httpServletRequest) {
        ActorContext actorContext = actorContextResolver.resolveRequired(httpServletRequest);
        return landlordStaffService.listStaff(actorContext, landlordId);
    }

    @PostMapping("/{landlordId}/staff")
    public StaffResponse inviteStaff(
            @PathVariable("landlordId") UUID landlordId,
            @Valid @RequestBody InviteStaffRequest request,
            HttpServletRequest httpServletRequest) {
        ActorContext actorContext = actorContextResolver.resolveRequired(httpServletRequest);
        return landlordStaffService.inviteStaff(actorContext, landlordId, request);
    }

    @PutMapping("/{landlordId}/staff/{staffId}/permissions")
    public StaffResponse replacePermissions(
            @PathVariable("landlordId") UUID landlordId,
            @PathVariable("staffId") UUID staffId,
            @Valid @RequestBody ReplaceStaffPermissionsRequest request,
            HttpServletRequest httpServletRequest) {
        ActorContext actorContext = actorContextResolver.resolveRequired(httpServletRequest);
        return landlordStaffService.replacePermissions(actorContext, landlordId, staffId, request);
    }

    @DeleteMapping("/{landlordId}/staff/{staffId}")
    public void deactivateStaff(
            @PathVariable("landlordId") UUID landlordId,
            @PathVariable("staffId") UUID staffId,
            HttpServletRequest httpServletRequest) {
        ActorContext actorContext = actorContextResolver.resolveRequired(httpServletRequest);
        landlordStaffService.deactivateStaff(actorContext, landlordId, staffId);
    }
}
