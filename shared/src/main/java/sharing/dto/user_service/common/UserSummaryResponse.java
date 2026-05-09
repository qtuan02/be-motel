package sharing.dto.user_service.common;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import sharing.enums.user_service.SensitivityClearance;
import sharing.enums.user_service.UserRole;

@Data
public class UserSummaryResponse {
    private UUID id;
    private String keycloakId;
    private UUID landlordId;
    private UserRole role;
    private String fullName;
    private String email;
    private String phoneMasked;
    private String avatarUrl;
    private SensitivityClearance sensitivityClearance;
    private Boolean isActive;
    private LocalDateTime lastLoginAt;
    private List<BuildingPermissionResponse> permissions = new ArrayList<>();
}
