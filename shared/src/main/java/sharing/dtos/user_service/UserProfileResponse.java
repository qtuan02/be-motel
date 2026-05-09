package sharing.dtos.user_service;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileResponse {
    private UUID id;
    private String code;
    private String keycloakId;
    private UUID landlordId;
    private String role;
    private String fullName;
    private String email;
    private String phone;
    private String avatarUrl;
    private String zaloUid;
    private String sensitivityClearance;
    private Boolean isActive;
    private LocalDateTime lastLoginAt;

    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
