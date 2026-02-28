package madp.auth.global.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private final String secret;
    private final Long accessExpiration;
    private final Long refreshExpiration;
    private final String keyId;
    private final String issuer;
}
