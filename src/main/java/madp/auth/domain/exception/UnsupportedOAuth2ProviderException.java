package madp.auth.domain.exception;

import madp.auth.global.exception.MadpBusinessException;
import org.springframework.http.HttpStatus;

public class UnsupportedOAuth2ProviderException extends MadpBusinessException {
    public UnsupportedOAuth2ProviderException(String provider) {
        super(provider + "기관의 OAuth2는 지원하지 않습니다.", HttpStatus.BAD_REQUEST);
    }
}
