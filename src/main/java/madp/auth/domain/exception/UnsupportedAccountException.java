package madp.auth.domain.exception;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

public class UnsupportedAccountException extends OAuth2AuthenticationException {
    public UnsupportedAccountException(String allowedDomain) {
        super(allowedDomain + " 이메일만 사용 가능합니다.");
    }
}
