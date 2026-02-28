package madp.auth.domain.domain.entity;

import lombok.*;
import madp.auth.domain.exception.InvalidAuthCodeInfoException;
import madp.auth.domain.exception.InvalidOAuth2SessionInfoException;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@RedisHash("oauth2_session")
@Getter
public class OAuth2SessionEntity {
    
    @Id
    private final String sessionKey;
    
    private final String userId;
    
    private final String userRole;

    @TimeToLive
    private final Long expiration;

    @Builder
    public OAuth2SessionEntity(String sessionKey, String userId, String userRole, Long timeToLive) {
        validateFields(sessionKey, userId, userRole, timeToLive);
        this.sessionKey = sessionKey;
        this.userId = userId;
        this.userRole = userRole;
        this.expiration = timeToLive;
    }

    private void validateFields(String sessionKey, String userId, String userRole, Long timeToLive) {
        if(sessionKey == null || sessionKey.isEmpty()) {
            throw new InvalidOAuth2SessionInfoException("세션 키는 비어 있을 수 없습니다.");
        }
        if(userId == null || userId.isEmpty()) {
            throw new InvalidOAuth2SessionInfoException("유저 아이디는 비어 있을 수 없습니다.");
        }
        if(userRole == null || userRole.isEmpty()) {
            throw new InvalidOAuth2SessionInfoException("역할은 비어 있을 수 없습니다.");
        }
        if(timeToLive == null || timeToLive < 0) {
            throw new InvalidOAuth2SessionInfoException("TTL 값은 0 이상이어야 합니다.");
        }
    }
}