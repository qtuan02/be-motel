package sharing.exceptions;

import java.util.Map;
import sharing.dtos.ErrorCode;

public class AppException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Map<String, Object> metadata;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.metadata = null;
    }

    public AppException(ErrorCode errorCode, Map<String, Object> metadata) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.metadata = metadata;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
