package madp.auth.domain.exception;

import madp.auth.global.exception.MadpBusinessException;
import org.springframework.http.HttpStatus;

public class InvalidAuthCodeInfoException extends MadpBusinessException {
    public InvalidAuthCodeInfoException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
