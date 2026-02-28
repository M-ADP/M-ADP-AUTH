package madp.auth.domain.exception;

import madp.auth.global.exception.resource.ResourceNotFoundException;

public class RefreshTokenNotFoundException extends ResourceNotFoundException {
    public RefreshTokenNotFoundException() {
        super("Refresh Token이 존재하지 않습니다.");
    }
}
