package sharing.dto.user_service.common;

import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;
import sharing.enums.user_service.UserPermission;

@Data
public class BuildingPermissionResponse {
    private UUID id;
    private UUID buildingId;
    private UserPermission permission;
    private LocalDate validFrom;
    private LocalDate validUntil;
}
