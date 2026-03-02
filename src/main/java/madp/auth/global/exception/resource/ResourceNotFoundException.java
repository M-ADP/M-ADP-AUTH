package madp.auth.global.exception.resource;

import madp.auth.global.exception.MadpBusinessException;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends MadpBusinessException {
    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
