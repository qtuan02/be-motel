package sharing.dto.user_service.auth;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import sharing.dto.user_service.common.UserSummaryResponse;

@Data
public class RegisterLandlordResponse {
    private UserSummaryResponse profile;
    private List<String> claims = new ArrayList<>();
}
