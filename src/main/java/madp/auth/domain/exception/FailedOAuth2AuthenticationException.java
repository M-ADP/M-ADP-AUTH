package madp.auth.domain.exception;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

public class FailedOAuth2AuthenticationException extends OAuth2AuthenticationException {
    public FailedOAuth2AuthenticationException() {
        super("OAuth2 인증에 실패했습니다.");
    }
}
