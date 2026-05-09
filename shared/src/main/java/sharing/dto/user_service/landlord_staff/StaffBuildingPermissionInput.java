package sharing.dto.user_service.landlord_staff;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;
import sharing.enums.user_service.UserPermission;

@Data
public class StaffBuildingPermissionInput {
    @NotNull(message = "Building ID is required") private UUID buildingId;

    @NotNull(message = "Permission is required") private UserPermission permission;

    private LocalDate validFrom;
    private LocalDate validUntil;
}
