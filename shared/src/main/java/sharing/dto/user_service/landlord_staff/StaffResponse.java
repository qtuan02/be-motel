package sharing.dto.user_service.landlord_staff;

import lombok.Data;
import sharing.dto.user_service.common.UserSummaryResponse;

@Data
public class StaffResponse {
    private UserSummaryResponse profile;
}
