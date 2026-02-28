package madp.auth.domain.exception;

import madp.auth.global.exception.resource.ResourceNotFoundException;

public class AuthCodeNotFoundException extends ResourceNotFoundException {
    public AuthCodeNotFoundException() {
        super("Auth Code가 존재하지 않습니다.");
    }
}
