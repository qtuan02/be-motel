package sharing.dto.user_service.landlord_staff;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ReplaceStaffPermissionsRequest {
    @NotNull(message = "Permissions list is required") @Valid private List<StaffBuildingPermissionInput> permissions = new ArrayList<>();
}
