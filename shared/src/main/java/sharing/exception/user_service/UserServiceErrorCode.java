package sharing.exception.user_service;

import org.springframework.http.HttpStatus;
import sharing.dto.ErrorCode;

public enum UserServiceErrorCode implements ErrorCode {
    USER_PROFILE_NOT_FOUND("USER_PROFILE_NOT_FOUND", "User profile not found", HttpStatus.NOT_FOUND),
    USER_EMAIL_ALREADY_EXISTS("USER_EMAIL_ALREADY_EXISTS", "Email already exists", HttpStatus.CONFLICT),
    USER_INACTIVE("USER_INACTIVE", "User is inactive", HttpStatus.FORBIDDEN),
    STAFF_ROLE_INVALID(
            "STAFF_ROLE_INVALID", "Only MANAGER or ACCOUNTANT roles are allowed for staff", HttpStatus.BAD_REQUEST),
    STAFF_PERMISSION_DUPLICATE_BUILDING(
            "STAFF_PERMISSION_DUPLICATE_BUILDING",
            "Duplicate building permission is not allowed for the same staff",
            HttpStatus.BAD_REQUEST),
    LANDLORD_ACCESS_DENIED(
            "LANDLORD_ACCESS_DENIED", "Actor does not have landlord access to this resource", HttpStatus.FORBIDDEN),
    PERMISSION_DATE_RANGE_INVALID(
            "PERMISSION_DATE_RANGE_INVALID", "Permission date range is invalid", HttpStatus.BAD_REQUEST),
    ABAC_POLICY_SYSTEM_READ_ONLY(
            "ABAC_POLICY_SYSTEM_READ_ONLY", "System ABAC policies are read only", HttpStatus.BAD_REQUEST),
    ABAC_POLICY_NOT_FOUND("ABAC_POLICY_NOT_FOUND", "ABAC policy not found", HttpStatus.NOT_FOUND),
    ABAC_POLICY_CONDITIONS_INVALID(
            "ABAC_POLICY_CONDITIONS_INVALID", "ABAC policy conditions must be valid JSON", HttpStatus.BAD_REQUEST),
    INTERNAL_AUTH_INVALID("INTERNAL_AUTH_INVALID", "Invalid internal authentication token", HttpStatus.UNAUTHORIZED),
    ACTOR_CONTEXT_MISSING("ACTOR_CONTEXT_MISSING", "Missing actor context", HttpStatus.UNAUTHORIZED),
    STAFF_NOT_FOUND("STAFF_NOT_FOUND", "Staff user not found", HttpStatus.NOT_FOUND),
    FCM_TOKEN_NOT_FOUND("FCM_TOKEN_NOT_FOUND", "FCM token not found", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus status;

    UserServiceErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }
}
