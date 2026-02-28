package madp.auth.global.exception.service;

import madp.auth.global.exception.MadpSystemError;
import org.springframework.http.HttpStatus;

public class ExternalServiceBadRequestException extends MadpSystemError {
    public ExternalServiceBadRequestException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
