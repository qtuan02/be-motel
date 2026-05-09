package sharing.dto.user_service.current_user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import sharing.enums.user_service.DeviceType;

@Data
public class RegisterFcmTokenRequest {
    @NotBlank(message = "FCM token is required") @Size(max = 500) private String token;

    @NotNull(message = "Device type is required") private DeviceType deviceType;
}
