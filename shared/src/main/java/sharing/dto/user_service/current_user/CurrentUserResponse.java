package sharing.dto.user_service.current_user;

import lombok.Data;
import sharing.dto.user_service.common.UserSummaryResponse;

@Data
public class CurrentUserResponse {
    private UserSummaryResponse profile;
    private boolean zaloLinked;
}
