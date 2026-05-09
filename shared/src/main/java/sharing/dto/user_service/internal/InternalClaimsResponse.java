package sharing.dto.user_service.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import sharing.enums.user_service.SensitivityClearance;
import sharing.enums.user_service.UserPermission;
import sharing.enums.user_service.UserRole;

@Data
public class InternalClaimsResponse {
    private String sub;
    private UserRole role;
    private UUID landlordId;
    private List<UUID> assignedBuildings = new ArrayList<>();
    private List<UserPermission> permissions = new ArrayList<>();
    private SensitivityClearance sensitivityClearance;
    private Boolean isActive;
}
