package madp.auth.domain.exception;

import madp.auth.global.exception.service.ExternalServiceBadRequestException;

public class InvalidOAuth2UserInformationException extends ExternalServiceBadRequestException {
  public InvalidOAuth2UserInformationException(String message) {
    super(message);
  }
}
