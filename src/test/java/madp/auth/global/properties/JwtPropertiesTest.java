package madp.auth.global.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JWT Properties 테스트")
class JwtPropertiesTest {

    @Test
    @DisplayName("JWT Properties를 생성하고 값들을 정상적으로 가져올 수 있다")
    void shouldCreateJwtPropertiesAndGetValues() {
        // Given: JWT 설정 정보가 준비되어 있다
        String secret = "test-secret-key";
        Long accessExpiration = 3600000L; // 1시간
        Long refreshExpiration = 86400000L; // 24시간

        // When: JwtProperties를 생성한다
        JwtProperties jwtProperties = new JwtProperties(secret, accessExpiration, refreshExpiration);

        // Then: 모든 값들이 정상적으로 설정된다
        assertThat(jwtProperties.getSecret()).isEqualTo(secret);
        assertThat(jwtProperties.getAccessExpiration()).isEqualTo(accessExpiration);
        assertThat(jwtProperties.getRefreshExpiration()).isEqualTo(refreshExpiration);
    }

    @Test
    @DisplayName("빈 문자열 시크릿으로 JWT Properties를 생성할 수 있다")
    void shouldCreateJwtPropertiesWithEmptySecret() {
        // Given: 빈 시크릿이 주어진다
        String emptySecret = "";
        Long accessExpiration = 1800000L; // 30분
        Long refreshExpiration = 43200000L; // 12시간

        // When: JwtProperties를 생성한다
        JwtProperties jwtProperties = new JwtProperties(emptySecret, accessExpiration, refreshExpiration);

        // Then: 빈 시크릿도 정상적으로 설정된다
        assertThat(jwtProperties.getSecret()).isEqualTo(emptySecret);
        assertThat(jwtProperties.getAccessExpiration()).isEqualTo(accessExpiration);
        assertThat(jwtProperties.getRefreshExpiration()).isEqualTo(refreshExpiration);
    }

    @Test
    @DisplayName("0 값의 만료 시간으로 JWT Properties를 생성할 수 있다")
    void shouldCreateJwtPropertiesWithZeroExpiration() {
        // Given: 0 값의 만료 시간이 주어진다
        String secret = "secret";
        Long zeroExpiration = 0L;

        // When: JwtProperties를 생성한다
        JwtProperties jwtProperties = new JwtProperties(secret, zeroExpiration, zeroExpiration);

        // Then: 0 값도 정상적으로 설정된다
        assertThat(jwtProperties.getSecret()).isEqualTo(secret);
        assertThat(jwtProperties.getAccessExpiration()).isEqualTo(zeroExpiration);
        assertThat(jwtProperties.getRefreshExpiration()).isEqualTo(zeroExpiration);
    }

    @Test
    @DisplayName("음수 만료 시간으로 JWT Properties를 생성할 수 있다")
    void shouldCreateJwtPropertiesWithNegativeExpiration() {
        // Given: 음수 만료 시간이 주어진다
        String secret = "secret";
        Long negativeExpiration = -1000L;

        // When: JwtProperties를 생성한다
        JwtProperties jwtProperties = new JwtProperties(secret, negativeExpiration, negativeExpiration);

        // Then: 음수 값도 정상적으로 설정된다
        assertThat(jwtProperties.getSecret()).isEqualTo(secret);
        assertThat(jwtProperties.getAccessExpiration()).isEqualTo(negativeExpiration);
        assertThat(jwtProperties.getRefreshExpiration()).isEqualTo(negativeExpiration);
    }

    @Test
    @DisplayName("매우 큰 만료 시간으로 JWT Properties를 생성할 수 있다")
    void shouldCreateJwtPropertiesWithLargeExpiration() {
        // Given: 매우 큰 만료 시간이 주어진다
        String secret = "secret";
        Long largeExpiration = Long.MAX_VALUE;

        // When: JwtProperties를 생성한다
        JwtProperties jwtProperties = new JwtProperties(secret, largeExpiration, largeExpiration);

        // Then: 큰 값도 정상적으로 설정된다
        assertThat(jwtProperties.getSecret()).isEqualTo(secret);
        assertThat(jwtProperties.getAccessExpiration()).isEqualTo(largeExpiration);
        assertThat(jwtProperties.getRefreshExpiration()).isEqualTo(largeExpiration);
    }

    @Test
    @DisplayName("null 값으로 JWT Properties를 생성할 수 있다")
    void shouldCreateJwtPropertiesWithNullValues() {
        // Given: null 값들이 주어진다
        String nullSecret = null;
        Long nullExpiration = null;

        // When: JwtProperties를 생성한다
        JwtProperties jwtProperties = new JwtProperties(nullSecret, nullExpiration, nullExpiration);

        // Then: null 값들도 정상적으로 설정된다
        assertThat(jwtProperties.getSecret()).isNull();
        assertThat(jwtProperties.getAccessExpiration()).isNull();
        assertThat(jwtProperties.getRefreshExpiration()).isNull();
    }

    @Test
    @DisplayName("서로 다른 액세스와 리프레시 토큰 만료 시간을 설정할 수 있다")
    void shouldCreateJwtPropertiesWithDifferentExpirations() {
        // Given: 서로 다른 만료 시간이 준비되어 있다
        String secret = "test-secret";
        Long accessExpiration = 900000L; // 15분
        Long refreshExpiration = 604800000L; // 7일

        // When: JwtProperties를 생성한다
        JwtProperties jwtProperties = new JwtProperties(secret, accessExpiration, refreshExpiration);

        // Then: 각기 다른 만료 시간이 정상적으로 설정된다
        assertThat(jwtProperties.getSecret()).isEqualTo(secret);
        assertThat(jwtProperties.getAccessExpiration()).isEqualTo(accessExpiration);
        assertThat(jwtProperties.getRefreshExpiration()).isEqualTo(refreshExpiration);
        assertThat(jwtProperties.getAccessExpiration()).isNotEqualTo(jwtProperties.getRefreshExpiration());
    }

    @Test
    @DisplayName("긴 시크릿 키로 JWT Properties를 생성할 수 있다")
    void shouldCreateJwtPropertiesWithLongSecret() {
        // Given: 긴 시크릿 키가 준비되어 있다
        String longSecret = "a".repeat(1000); // 1000자의 시크릿
        Long accessExpiration = 3600000L;
        Long refreshExpiration = 86400000L;

        // When: JwtProperties를 생성한다
        JwtProperties jwtProperties = new JwtProperties(longSecret, accessExpiration, refreshExpiration);

        // Then: 긴 시크릿도 정상적으로 설정된다
        assertThat(jwtProperties.getSecret()).isEqualTo(longSecret);
        assertThat(jwtProperties.getSecret()).hasSize(1000);
        assertThat(jwtProperties.getAccessExpiration()).isEqualTo(accessExpiration);
        assertThat(jwtProperties.getRefreshExpiration()).isEqualTo(refreshExpiration);
    }
}