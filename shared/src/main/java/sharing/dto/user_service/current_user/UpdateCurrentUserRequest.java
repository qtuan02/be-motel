package sharing.dto.user_service.current_user;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCurrentUserRequest {
    @Size(max = 200) private String fullName;

    @Size(max = 500) private String avatarUrl;

    @Size(max = 30) private String phone;

    @Size(max = 100) private String zaloUid;
}
