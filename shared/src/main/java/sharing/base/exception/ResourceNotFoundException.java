package sharing.base.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String entity, String field, Object value) {
        super(String.format("%s not found with %s: '%s'", entity, field, value));
    }
}
