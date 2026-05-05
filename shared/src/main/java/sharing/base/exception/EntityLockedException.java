package sharing.base.exception;

public class EntityLockedException extends RuntimeException {

    public EntityLockedException() {
        super("Entity is locked and cannot be modified or deleted.");
    }
}
