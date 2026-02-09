package madp.auth.domain.infrastructure.client;

import madp.auth.domain.infrastructure.client.fallback.UserClientFallback;
import madp.auth.domain.infrastructure.client.request.OAuth2UserInformationRequestDto;
import madp.auth.domain.infrastructure.client.response.UserAuthResponseDto;
import madp.auth.global.configuration.InternalServiceCommunicationConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "user-client",
        fallback = UserClientFallback.class,
        configuration = InternalServiceCommunicationConfiguration.class
)
public interface UserClient {
    @PostMapping("/user/auth-status")
    UserAuthResponseDto getUserAuthStatus(@RequestBody OAuth2UserInformationRequestDto oAuth2UserInformationRequestDto);

}
