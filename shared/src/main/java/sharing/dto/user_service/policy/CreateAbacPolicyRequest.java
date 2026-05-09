package sharing.dto.user_service.policy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import sharing.enums.user_service.PolicyEffect;

@Data
public class CreateAbacPolicyRequest {
    @NotBlank(message = "Policy name is required") @Size(max = 120) private String name;

    @NotBlank(message = "Resource is required") @Size(max = 80) private String resource;

    @NotBlank(message = "Action is required") @Size(max = 80) private String action;

    @NotNull(message = "Policy effect is required") private PolicyEffect policyEffect;

    @NotBlank(message = "Conditions JSON is required") private String conditions;
}
