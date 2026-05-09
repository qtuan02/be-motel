package sharing.dto.user_service.policy;

import java.util.UUID;
import lombok.Data;
import sharing.enums.user_service.PolicyEffect;

@Data
public class AbacPolicyResponse {
    private UUID id;
    private UUID landlordId;
    private String name;
    private String resource;
    private String action;
    private PolicyEffect policyEffect;
    private String conditions;
    private Boolean systemPolicy;
}
