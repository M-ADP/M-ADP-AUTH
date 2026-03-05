package madp.auth.global.configuration;

import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import madp.auth.domain.domain.repository.OAuth2SessionRepository;
import madp.auth.global.infrastructure.internal.InternalRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(FeignClientConfiguration.class)
@RequiredArgsConstructor
public class InternalServiceCommunicationConfiguration {
    private final OAuth2SessionRepository oAuth2SessionRepository;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new InternalRequestInterceptor(oAuth2SessionRepository);
    }
}
