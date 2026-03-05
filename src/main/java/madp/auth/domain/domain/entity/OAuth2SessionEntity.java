package madp.auth.domain.domain.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

@Getter
@RedisHash("oauth2_session")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuth2SessionEntity {
    
    @Id
    private String sessionId;
    
    private Long userId;
    private String userRole;
    private OAuth2AuthorizationRequest oAuth2AuthorizationRequest;
    
    @TimeToLive
    private Long expiration;

    @Builder
    public OAuth2SessionEntity(String sessionId, OAuth2AuthorizationRequest oAuth2AuthorizationRequest, Long userId, String userRole, Long expiration) {
        this.sessionId = sessionId;
        this.oAuth2AuthorizationRequest = oAuth2AuthorizationRequest;
        this.userId = userId;
        this.userRole = userRole;
        this.expiration = expiration;
    }
}
