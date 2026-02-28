package madp.auth.domain.exception;

import madp.auth.global.exception.service.ExternalServiceUnavailableException;

public class UserServiceUnavailableException extends ExternalServiceUnavailableException {
    public UserServiceUnavailableException() {
        super("사용자");
    }
}
