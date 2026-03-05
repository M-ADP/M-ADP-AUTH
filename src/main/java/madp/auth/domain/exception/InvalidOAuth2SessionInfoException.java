package madp.auth.domain.exception;

import madp.auth.global.exception.MadpBusinessException;
import org.springframework.http.HttpStatus;

public class InvalidOAuth2SessionInfoException extends MadpBusinessException {
    public InvalidOAuth2SessionInfoException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}