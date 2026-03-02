package madp.auth.global.configuration;

import feign.RequestInterceptor;
import madp.auth.global.infrastructure.internal.InternalRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(FeignClientConfiguration.class)
public class InternalServiceCommunicationConfiguration {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new InternalRequestInterceptor();
    }
}
