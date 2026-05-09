package sharing.dto.user_service.current_user;

import java.util.UUID;
import lombok.Data;
import sharing.enums.user_service.DeviceType;

@Data
public class FcmTokenResponse {
    private UUID id;
    private DeviceType deviceType;
    private Boolean isActive;
}
