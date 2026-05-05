package sharing.exception;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import sharing.dto.ErrorCode;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String BASE_ERROR_URI = "https://api.motel.com/errors/";

    @Value("${spring.application.name:unknown-service}")
    private String serviceName;

    @ExceptionHandler(AppException.class)
    public ProblemDetail handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(errorCode.getStatus(), errorCode.getMessage());

        problemDetail.setTitle("Business Logic Error");
        problemDetail.setType(URI.create(BASE_ERROR_URI + errorCode.getCode()));
        problemDetail.setProperty("code", errorCode.getCode());

        if (ex.getMetadata() != null && !ex.getMetadata().isEmpty()) {
            problemDetail.setProperty("metadata", ex.getMetadata());
        }

        addCommonProperties(problemDetail);
        return problemDetail;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        String errorDetails = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, "Invalid input data");
        problemDetail.setTitle("Validation Error");
        problemDetail.setType(URI.create(BASE_ERROR_URI + "validation-failed"));
        problemDetail.setProperty("code", "VALIDATION_FAILED");
        problemDetail.setProperty("metadata", Map.of("fields", errorDetails));

        addCommonProperties(problemDetail);
        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneralException(Exception ex) {
        log.error("Unhandled exception in service: {}", serviceName, ex);

        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500), "An unexpected server error occurred.");

        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(URI.create(BASE_ERROR_URI + "internal-error"));
        problemDetail.setProperty("code", "INTERNAL_SERVER_ERROR");

        addCommonProperties(problemDetail);
        return problemDetail;
    }

    private void addCommonProperties(ProblemDetail problemDetail) {
        problemDetail.setProperty("service", serviceName);
        problemDetail.setProperty("timestamp", Instant.now());
    }
}
