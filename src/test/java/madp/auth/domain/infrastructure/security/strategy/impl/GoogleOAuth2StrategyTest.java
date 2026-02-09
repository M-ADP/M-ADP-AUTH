package madp.auth.domain.infrastructure.security.strategy.impl;

import madp.auth.domain.domain.enums.OAuth2Type;
import madp.auth.domain.exception.UnsupportedAccountException;
import madp.auth.domain.infrastructure.security.vo.MadpOAuth2UserInfo;
import madp.auth.global.properties.EmailProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Google OAuth2 전략 테스트")
class GoogleOAuth2StrategyTest {

    @Mock
    private EmailProperties emailProperties;

    @Mock
    private OAuth2User oauth2User;

    private GoogleOAuth2Strategy googleOAuth2Strategy;

    @BeforeEach
    void setUp() {
        googleOAuth2Strategy = new GoogleOAuth2Strategy(emailProperties);
    }

    @Test
    @DisplayName("OAuth2 프로바이더 타입이 GOOGLE로 반환된다")
    void shouldReturnGoogleProviderType() {
        // When: OAuth2 프로바이더 타입을 조회한다
        OAuth2Type result = googleOAuth2Strategy.getOAuth2ProviderType();

        // Then: GOOGLE 타입이 반환된다
        assertThat(result).isEqualTo(OAuth2Type.GOOGLE);
    }

    @Test
    @DisplayName("허용된 도메인 이메일로 사용자 정보 추출 시 성공한다")
    void shouldGetUserInfoSuccessfullyWithAllowedEmail() {
        // Given: 허용된 도메인의 Google 사용자 정보가 준비되어 있다
        String allowedDomain = "@example.com";
        Map<String, Object> attributes = Map.of(
                "sub", "google-user-id",
                "email", "user@example.com",
                "picture", "https://example.com/profile.jpg",
                "name", "Test User"
        );
        
        given(oauth2User.getAttributes()).willReturn(attributes);
        given(emailProperties.getAllowedDomain()).willReturn(allowedDomain);

        // When: 사용자 정보를 추출한다
        MadpOAuth2UserInfo result = googleOAuth2Strategy.getUserInfo(oauth2User);

        // Then: 사용자 정보가 성공적으로 추출된다
        assertThat(result).isNotNull();
        assertThat(result.providerId()).isEqualTo("google-user-id");
        assertThat(result.mail()).isEqualTo("user@example.com");
        assertThat(result.profile()).isEqualTo("https://example.com/profile.jpg");
        assertThat(result.name()).isEqualTo("Test User");
        assertThat(result.oAuth2Type()).isEqualTo(OAuth2Type.GOOGLE);
    }

    @Test
    @DisplayName("허용되지 않은 도메인 이메일 시 UnsupportedAccountException이 발생한다")
    void shouldThrowUnsupportedAccountExceptionWhenEmailDomainNotAllowed() {
        // Given: 허용되지 않은 도메인의 이메일이 주어진다
        String allowedDomain = "@example.com";
        Map<String, Object> attributes = Map.of(
                "sub", "google-user-id",
                "email", "user@forbidden.com",
                "picture", "https://example.com/profile.jpg",
                "name", "Test User"
        );
        
        given(oauth2User.getAttributes()).willReturn(attributes);
        given(emailProperties.getAllowedDomain()).willReturn(allowedDomain);

        // When & Then: 사용자 정보 추출 시 예외가 발생한다
        assertThatThrownBy(() -> googleOAuth2Strategy.getUserInfo(oauth2User))
                .isInstanceOf(UnsupportedAccountException.class);
    }

    @Test
    @DisplayName("이메일이 null인 경우 UnsupportedAccountException이 발생한다")
    void shouldThrowUnsupportedAccountExceptionWhenEmailIsNull() {
        // Given: 이메일이 null인 사용자 정보가 주어진다
        String allowedDomain = "@example.com";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "google-user-id");
        attributes.put("email", null);
        attributes.put("picture", "https://example.com/profile.jpg");
        attributes.put("name", "Test User");
        
        given(oauth2User.getAttributes()).willReturn(attributes);
        given(emailProperties.getAllowedDomain()).willReturn(allowedDomain);

        // When & Then: 사용자 정보 추출 시 UnsupportedAccountException이 발생한다
        assertThatThrownBy(() -> googleOAuth2Strategy.getUserInfo(oauth2User))
                .isInstanceOf(UnsupportedAccountException.class);
    }

    @Test
    @DisplayName("허용된 도메인으로 끝나는 이메일인지 정확히 검증한다")
    void shouldValidateEmailDomainCorrectly() {
        // Given: 허용된 도메인이 설정되어 있다
        String allowedDomain = "@company.com";
        Map<String, Object> validAttributes = Map.of(
                "sub", "user-id",
                "email", "john.doe@company.com",
                "picture", "profile.jpg",
                "name", "John Doe"
        );
        Map<String, Object> invalidAttributes = Map.of(
                "sub", "user-id",
                "email", "john.doe@othercompany.com",
                "picture", "profile.jpg",
                "name", "John Doe"
        );
        
        given(emailProperties.getAllowedDomain()).willReturn(allowedDomain);

        // When & Then: 유효한 이메일은 성공한다
        given(oauth2User.getAttributes()).willReturn(validAttributes);
        assertThatCode(() -> googleOAuth2Strategy.getUserInfo(oauth2User))
                .doesNotThrowAnyException();

        // When & Then: 유효하지 않은 이메일은 예외가 발생한다
        given(oauth2User.getAttributes()).willReturn(invalidAttributes);
        assertThatThrownBy(() -> googleOAuth2Strategy.getUserInfo(oauth2User))
                .isInstanceOf(UnsupportedAccountException.class);
    }
}