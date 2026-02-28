package madp.auth.domain.exception;

import madp.auth.global.exception.MadpBusinessException;
import org.springframework.http.HttpStatus;

public class InvalidTokenInfoException extends MadpBusinessException {
    public InvalidTokenInfoException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
