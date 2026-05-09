package sharing.dto.user_service.current_user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateZaloUidRequest {
    @NotBlank(message = "Zalo UID is required") @Size(max = 100) private String zaloUid;
}
