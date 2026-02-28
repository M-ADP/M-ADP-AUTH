package madp.auth.domain.infrastructure.client.fallback;

import madp.auth.domain.exception.UserServiceUnavailableException;
import madp.auth.domain.infrastructure.client.UserClient;
import madp.auth.domain.infrastructure.client.request.OAuth2UserInformationRequestDto;
import madp.auth.domain.infrastructure.client.response.UserAuthResponseDto;
import org.springframework.stereotype.Component;

@Component
public class UserClientFallback implements UserClient {

    @Override
    public UserAuthResponseDto getUserAuthStatus(OAuth2UserInformationRequestDto oAuth2UserInformationRequestDto) {
        throw new UserServiceUnavailableException();
    }
}
