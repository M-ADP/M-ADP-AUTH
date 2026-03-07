package madp.auth.domain.domain.entity;

import lombok.Builder;
import lombok.Getter;
import madp.auth.domain.exception.InvalidTokenInfoException;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@RedisHash(value = "token")
public class TokenEntity {
    @Id
    private final Long userId;

    private String token;

    @TimeToLive
    private final Long expiration;

    @Builder
    public TokenEntity(Long userId, String token, Long expiration) {
        validateUserIdField(userId);
        validateTokenField(token);
        validateExpirationField(expiration);

        this.userId = userId;
        this.token = token;
        this.expiration = expiration;
    }

    private void validateUserIdField(Long userId) {
        if(userId == null)
            throw new InvalidTokenInfoException("유저 아이디는 비어 있을 수 없습니다.");
    }

    private void validateTokenField(String token) {
        if(token == null || token.trim().isEmpty())
            throw new InvalidTokenInfoException("토큰은 비어 있을 수 없습니다.");
    }

    private void validateExpirationField(Long expiration) {
        if(expiration == null || expiration < 0)
            throw new InvalidTokenInfoException("expiration 값은 0 이상이어야 합니다.");
    }

    public void updateToken(String refreshToken) {
        this.token = refreshToken;
    }

}