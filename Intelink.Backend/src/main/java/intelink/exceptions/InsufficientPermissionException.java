package intelink.exceptions;

import org.springframework.security.access.AccessDeniedException;

public class InsufficientPermissionException extends AccessDeniedException {
    public InsufficientPermissionException(String message) {
        super(message);
    }
}