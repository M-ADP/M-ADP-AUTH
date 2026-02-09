package madp.auth.domain.application.service;

import madp.auth.domain.domain.entity.AuthCodeEntity;
import madp.auth.domain.domain.entity.TokenEntity;
import madp.auth.domain.domain.enums.Role;
import madp.auth.domain.domain.repository.AuthCodeRepository;
import madp.auth.domain.domain.repository.TokenRepository;
import madp.auth.domain.exception.AuthCodeNotFoundException;
import madp.auth.domain.infrastructure.jwt.JwtManager;
import madp.auth.domain.presentation.dto.response.TokenResponseDto;
import madp.auth.global.properties.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auth 서비스 테스트")
class AuthServiceTest {

    @Mock
    private JwtManager jwtManager;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private AuthCodeRepository authCodeRepository;

    private AuthService authService;
    private AuthCodeEntity authCodeEntity;

    @BeforeEach
    void setUp() {
        authService = new AuthService(jwtManager, jwtProperties, tokenRepository, authCodeRepository);
        
        authCodeEntity = AuthCodeEntity.builder()
                .authCode("test-auth-code")
                .accessToken("test-access-token")
                .timeToLive(300L)
                .build();
    }

    @Test
    @DisplayName("리프레시 토큰 삭제 시 삭제된 쿠키가 성공적으로 반환된다")
    void shouldDeleteRefreshTokenSuccessfully() {
        // Given: 리프레시 토큰과 삭제된 쿠키가 준비되어 있다
        String refreshToken = "test-refresh-token";
        ResponseCookie deletedCookie = ResponseCookie.from("refreshToken", "")
                .maxAge(0)
                .build();
        given(jwtManager.createDeletedRefreshTokenCookie()).willReturn(deletedCookie);

        // When: 리프레시 토큰을 삭제한다
        ResponseCookie result = authService.deleteRefreshToken(refreshToken);

        // Then: 삭제된 쿠키가 반환되고 토큰이 저장소에서 삭제된다
        assertThat(result).isNotNull();
        assertThat(result.getValue()).isEmpty();
        assertThat(result.getMaxAge().getSeconds()).isEqualTo(0);
        
        verify(tokenRepository).deleteById(refreshToken);
        verify(jwtManager).createDeletedRefreshTokenCookie();
    }

    @Test
    @DisplayName("토큰 재발급 시 새로운 토큰이 성공적으로 생성된다")
    void shouldReissueTokenSuccessfully() {
        // Given: 기존 리프레시 토큰과 새 토큰 정보가 준비되어 있다
        String oldRefreshToken = "old-refresh-token";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";
        Long userId = 1L;
        Role role = Role.USER;
        long refreshExpiration = 86400L;
        
        ResponseCookie newRefreshTokenCookie = ResponseCookie.from("refreshToken", newRefreshToken)
                .maxAge(refreshExpiration)
                .build();

        given(jwtManager.getUserId(oldRefreshToken)).willReturn(userId);
        given(jwtManager.getRole(oldRefreshToken)).willReturn(role);
        given(jwtManager.generateAccessToken(userId, role)).willReturn(newAccessToken);
        given(jwtManager.generateRefreshToken(userId, role)).willReturn(newRefreshToken);
        given(jwtProperties.getRefreshExpiration()).willReturn(refreshExpiration);
        given(jwtManager.createRefreshTokenCookie(newRefreshToken)).willReturn(newRefreshTokenCookie);

        // When: 토큰을 재발급한다
        TokenResponseDto result = authService.reissueToken(oldRefreshToken);

        // Then: 새로운 토큰이 성공적으로 생성되고 기존 토큰은 삭제된다
        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo(newAccessToken);
        assertThat(result.refreshTokenCookie()).isEqualTo(newRefreshTokenCookie);
        
        verify(tokenRepository).deleteById(oldRefreshToken);
        verify(tokenRepository).save(any(TokenEntity.class));
        verify(jwtManager).getUserId(oldRefreshToken);
        verify(jwtManager).getRole(oldRefreshToken);
        verify(jwtManager).generateAccessToken(userId, role);
        verify(jwtManager).generateRefreshToken(userId, role);
    }

    @Test
    @DisplayName("인증 코드로 액세스 토큰 조회 시 토큰이 성공적으로 반환된다")
    void shouldGetAccessTokenByAuthCodeSuccessfully() {
        // Given: 유효한 인증 코드가 존재한다
        String authCode = "test-auth-code";
        String expectedAccessToken = "test-access-token";
        given(authCodeRepository.findByAuthCode(authCode)).willReturn(Optional.of(authCodeEntity));

        // When: 인증 코드로 액세스 토큰을 조회한다
        String result = authService.getAccessTokenByAuthCode(authCode);

        // Then: 액세스 토큰이 반환되고 인증 코드는 삭제된다
        assertThat(result).isEqualTo(expectedAccessToken);
        
        verify(authCodeRepository).findByAuthCode(authCode);
        verify(authCodeRepository).delete(authCodeEntity);
    }

    @Test
    @DisplayName("존재하지 않는 인증 코드로 조회 시 AuthCodeNotFoundException이 발생한다")
    void shouldThrowAuthCodeNotFoundExceptionWhenAuthCodeNotExists() {
        // Given: 존재하지 않는 인증 코드가 주어진다
        String invalidAuthCode = "invalid-auth-code";
        given(authCodeRepository.findByAuthCode(invalidAuthCode)).willReturn(Optional.empty());

        // When & Then: 인증 코드 조회 시 예외가 발생한다
        assertThatThrownBy(() -> authService.getAccessTokenByAuthCode(invalidAuthCode))
                .isInstanceOf(AuthCodeNotFoundException.class);

        verify(authCodeRepository).findByAuthCode(invalidAuthCode);
        verify(authCodeRepository, never()).delete(any());
    }
}