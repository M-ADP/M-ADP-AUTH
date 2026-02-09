package madp.auth.domain.infrastructure.jwt;

import madp.auth.domain.domain.enums.Role;
import madp.auth.domain.infrastructure.jwt.constants.JwtConstants;
import madp.auth.global.properties.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.server.Cookie;
import org.springframework.http.ResponseCookie;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JWT 관리자 테스트")
class JwtManagerTest {

    private JwtProperties jwtProperties;
    private JwtManager jwtManager;

    @BeforeEach
    void setUp() {
        // 실제 프로퍼티 객체 생성
        jwtProperties = new JwtProperties(
                "test-secret-key-for-jwt-manager-unit-test-minimum-256-bits-required",
                3600000L, // 1시간
                86400000L // 24시간
        );
        
        jwtManager = new JwtManager(jwtProperties);
    }

    @Test
    @DisplayName("액세스 토큰을 성공적으로 생성할 수 있다")
    void shouldGenerateAccessTokenSuccessfully() {
        // Given: 사용자 ID와 권한이 주어진다
        Long userId = 1L;
        Role role = Role.USER;

        // When: 액세스 토큰을 생성한다
        String accessToken = jwtManager.generateAccessToken(userId, role);

        // Then: 토큰이 성공적으로 생성된다
        assertThat(accessToken).isNotNull();
        assertThat(accessToken).isNotEmpty();
        assertThat(accessToken.split("\\.")).hasSize(3); // JWT는 3개 부분으로 구성
    }

    @Test
    @DisplayName("리프레시 토큰을 성공적으로 생성할 수 있다")
    void shouldGenerateRefreshTokenSuccessfully() {
        // Given: 사용자 ID와 권한이 주어진다
        Long userId = 2L;
        Role role = Role.PARTIAL_AUTH;

        // When: 리프레시 토큰을 생성한다
        String refreshToken = jwtManager.generateRefreshToken(userId, role);

        // Then: 토큰이 성공적으로 생성된다
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();
        assertThat(refreshToken.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("생성된 토큰에서 사용자 ID를 추출할 수 있다")
    void shouldExtractUserIdFromGeneratedToken() {
        // Given: 토큰이 생성되어 있다
        Long expectedUserId = 123L;
        Role role = Role.USER;
        String token = jwtManager.generateAccessToken(expectedUserId, role);

        // When: 토큰에서 사용자 ID를 추출한다
        Long actualUserId = jwtManager.getUserId(token);

        // Then: 올바른 사용자 ID가 추출된다
        assertThat(actualUserId).isEqualTo(expectedUserId);
    }

    @Test
    @DisplayName("생성된 토큰에서 권한을 추출할 수 있다")
    void shouldExtractRoleFromGeneratedToken() {
        // Given: 토큰이 생성되어 있다
        Long userId = 456L;
        Role expectedRole = Role.PARTIAL_AUTH;
        String token = jwtManager.generateRefreshToken(userId, expectedRole);

        // When: 토큰에서 권한을 추출한다
        Role actualRole = jwtManager.getRole(token);

        // Then: 올바른 권한이 추출된다
        assertThat(actualRole).isEqualTo(expectedRole);
    }

    @Test
    @DisplayName("리프레시 토큰 쿠키를 성공적으로 생성할 수 있다")
    void shouldCreateRefreshTokenCookieSuccessfully() {
        // Given: 리프레시 토큰이 준비되어 있다
        String refreshToken = "test-refresh-token";

        // When: 리프레시 토큰 쿠키를 생성한다
        ResponseCookie cookie = jwtManager.createRefreshTokenCookie(refreshToken);

        // Then: 쿠키가 올바르게 생성된다
        assertThat(cookie.getName()).isEqualTo(JwtConstants.REFRESH_TOKEN_COOKIE_HEADER);
        assertThat(cookie.getValue()).isEqualTo(refreshToken);
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.isSecure()).isTrue();
        assertThat(cookie.getSameSite()).isEqualTo(Cookie.SameSite.STRICT.attributeValue());
        assertThat(cookie.getMaxAge().getSeconds()).isEqualTo(jwtProperties.getRefreshExpiration() / 1000);
    }

    @Test
    @DisplayName("삭제된 리프레시 토큰 쿠키를 성공적으로 생성할 수 있다")
    void shouldCreateDeletedRefreshTokenCookieSuccessfully() {
        // When: 삭제된 리프레시 토큰 쿠키를 생성한다
        ResponseCookie deletedCookie = jwtManager.createDeletedRefreshTokenCookie();

        // Then: 삭제 용도의 쿠키가 올바르게 생성된다
        assertThat(deletedCookie.getName()).isEqualTo(JwtConstants.REFRESH_TOKEN_COOKIE_HEADER);
        assertThat(deletedCookie.getValue()).isEmpty();
        assertThat(deletedCookie.getPath()).isEqualTo("/");
        assertThat(deletedCookie.isHttpOnly()).isTrue();
        assertThat(deletedCookie.isSecure()).isTrue();
        assertThat(deletedCookie.getSameSite()).isEqualTo(Cookie.SameSite.STRICT.attributeValue());
        assertThat(deletedCookie.getMaxAge().getSeconds()).isEqualTo(0L);
    }

    @Test
    @DisplayName("여러 사용자의 토큰을 생성하고 각각 올바르게 구분할 수 있다")
    void shouldGenerateAndDistinguishMultipleUserTokens() {
        // Given: 여러 사용자 정보가 준비되어 있다
        Long userId1 = 100L;
        Long userId2 = 200L;
        Role role1 = Role.USER;
        Role role2 = Role.PARTIAL_AUTH;

        // When: 여러 사용자의 토큰을 생성한다
        String token1 = jwtManager.generateAccessToken(userId1, role1);
        String token2 = jwtManager.generateAccessToken(userId2, role2);

        // Then: 각 토큰에서 올바른 사용자 정보가 추출된다
        assertThat(jwtManager.getUserId(token1)).isEqualTo(userId1);
        assertThat(jwtManager.getRole(token1)).isEqualTo(role1);
        
        assertThat(jwtManager.getUserId(token2)).isEqualTo(userId2);
        assertThat(jwtManager.getRole(token2)).isEqualTo(role2);
        
        // 토큰들은 서로 다르다
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    @DisplayName("동일한 사용자 정보로 생성된 토큰들은 생성 시점이 달라 서로 다르다")
    void shouldGenerateDifferentTokensForSameUserAtDifferentTimes() throws InterruptedException {
        // Given: 동일한 사용자 정보가 준비되어 있다
        Long userId = 999L;
        Role role = Role.USER;

        // When: 시간 간격을 두고 토큰을 생성한다
        String token1 = jwtManager.generateAccessToken(userId, role);
        Thread.sleep(1100); // 1초 이상 대기하여 iat 클레임 차이 보장
        String token2 = jwtManager.generateAccessToken(userId, role);

        // Then: 토큰들은 서로 다르지만 같은 사용자 정보를 포함한다
        assertThat(token1).isNotEqualTo(token2);
        assertThat(jwtManager.getUserId(token1)).isEqualTo(jwtManager.getUserId(token2));
        assertThat(jwtManager.getRole(token1)).isEqualTo(jwtManager.getRole(token2));
    }

    @Test
    @DisplayName("액세스 토큰과 리프레시 토큰은 같은 정보를 담고 있지만 서로 다르다")
    void shouldGenerateDifferentAccessAndRefreshTokens() {
        // Given: 사용자 정보가 준비되어 있다
        Long userId = 777L;
        Role role = Role.USER;

        // When: 액세스 토큰과 리프레시 토큰을 생성한다
        String accessToken = jwtManager.generateAccessToken(userId, role);
        String refreshToken = jwtManager.generateRefreshToken(userId, role);

        // Then: 토큰들은 서로 다르지만 같은 사용자 정보를 포함한다
        assertThat(accessToken).isNotEqualTo(refreshToken);
        assertThat(jwtManager.getUserId(accessToken)).isEqualTo(jwtManager.getUserId(refreshToken));
        assertThat(jwtManager.getRole(accessToken)).isEqualTo(jwtManager.getRole(refreshToken));
    }

    @Test
    @DisplayName("잘못된 토큰으로 사용자 ID 추출 시 예외가 발생한다")
    void shouldThrowExceptionWhenExtractingUserIdFromInvalidToken() {
        // Given: 잘못된 토큰이 주어진다
        String invalidToken = "invalid.jwt.token";

        // When & Then: 사용자 ID 추출 시 예외가 발생한다
        assertThatThrownBy(() -> jwtManager.getUserId(invalidToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("잘못된 토큰으로 권한 추출 시 예외가 발생한다")
    void shouldThrowExceptionWhenExtractingRoleFromInvalidToken() {
        // Given: 잘못된 토큰이 주어진다
        String invalidToken = "invalid.jwt.token";

        // When & Then: 권한 추출 시 예외가 발생한다
        assertThatThrownBy(() -> jwtManager.getRole(invalidToken))
                .isInstanceOf(Exception.class);
    }
}