package com.motel.user_service.dto.user_profile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Data;

@Data
public class UserProfileRequest {
    private String code;

    @NotBlank(message = "Keycloak ID is required") private String keycloakId;

    private UUID landlordId;

    @NotBlank(message = "Role is required") private String role;

    @NotBlank(message = "Full name is required") private String fullName;

    @NotBlank(message = "Email is required") @Email(message = "Invalid email format") private String email;

    private String phone;
    private String avatarUrl;
    private String zaloUid;
    private String sensitivityClearance;
    private Boolean isActive;
}
