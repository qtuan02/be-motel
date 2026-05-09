package sharing.dto.user_service.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterLandlordRequest {
    @NotBlank(message = "Email is required") @Email(message = "Email format is invalid") @Size(max = 200) private String email;

    @NotBlank(message = "Password is required") @Size(min = 8, max = 100) private String password;

    @NotBlank(message = "Full name is required") @Size(max = 200) private String fullName;

    @Size(max = 30) private String phone;

    @Size(max = 500) private String avatarUrl;
}
