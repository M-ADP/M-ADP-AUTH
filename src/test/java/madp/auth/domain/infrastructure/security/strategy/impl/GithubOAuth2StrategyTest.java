package madp.auth.domain.infrastructure.security.strategy.impl;

import madp.auth.domain.domain.enums.OAuth2Type;
import madp.auth.domain.infrastructure.security.vo.MadpOAuth2UserInfo;
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
@DisplayName("Github OAuth2 전략 테스트")
class GithubOAuth2StrategyTest {

    @Mock
    private OAuth2User oauth2User;

    private GithubOAuth2Strategy githubOAuth2Strategy;

    @BeforeEach
    void setUp() {
        githubOAuth2Strategy = new GithubOAuth2Strategy();
    }

    @Test
    @DisplayName("OAuth2 프로바이더 타입이 GITHUB로 반환된다")
    void shouldReturnGithubProviderType() {
        // When: OAuth2 프로바이더 타입을 조회한다
        OAuth2Type result = githubOAuth2Strategy.getOAuth2ProviderType();

        // Then: GITHUB 타입이 반환된다
        assertThat(result).isEqualTo(OAuth2Type.GITHUB);
    }

    @Test
    @DisplayName("GitHub 사용자 정보를 성공적으로 추출할 수 있다")
    void shouldGetUserInfoSuccessfullyFromGitHub() {
        // Given: GitHub 사용자 정보가 준비되어 있다
        Map<String, Object> attributes = Map.of(
                "login", "github-username",
                "email", "user@example.com",
                "avatar_url", "https://github.com/avatar.jpg",
                "name", "Test User"
        );
        
        given(oauth2User.getAttributes()).willReturn(attributes);

        // When: 사용자 정보를 추출한다
        MadpOAuth2UserInfo result = githubOAuth2Strategy.getUserInfo(oauth2User);

        // Then: 사용자 정보가 성공적으로 추출된다
        assertThat(result).isNotNull();
        assertThat(result.providerId()).isEqualTo("github-username");
        assertThat(result.mail()).isEqualTo("user@example.com");
        assertThat(result.profile()).isEqualTo("https://github.com/avatar.jpg");
        assertThat(result.name()).isEqualTo("Test User");
        assertThat(result.oAuth2Type()).isEqualTo(OAuth2Type.GITHUB);
    }

    @Test
    @DisplayName("다른 이메일 도메인으로도 사용자 정보를 추출할 수 있다")
    void shouldGetUserInfoWithDifferentEmailDomains() {
        // Given: 다른 도메인의 GitHub 사용자 정보가 준비되어 있다
        Map<String, Object> attributes = Map.of(
                "login", "github-username",
                "email", "user@company.com",
                "avatar_url", "https://github.com/avatar.jpg",
                "name", "Test User"
        );
        
        given(oauth2User.getAttributes()).willReturn(attributes);

        // When: 사용자 정보를 추출한다
        MadpOAuth2UserInfo result = githubOAuth2Strategy.getUserInfo(oauth2User);

        // Then: 사용자 정보가 성공적으로 추출된다
        assertThat(result).isNotNull();
        assertThat(result.mail()).isEqualTo("user@company.com");
        assertThat(result.oAuth2Type()).isEqualTo(OAuth2Type.GITHUB);
    }

    @Test
    @DisplayName("이메일이 null인 경우 NullPointerException이 발생한다")
    void shouldThrowNullPointerExceptionWhenEmailIsNull() {
        // Given: 이메일이 null인 사용자 정보가 주어진다
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("login", "github-username");
        attributes.put("email", null);
        attributes.put("avatar_url", "https://github.com/avatar.jpg");
        attributes.put("name", "Test User");
        
        given(oauth2User.getAttributes()).willReturn(attributes);

        // When & Then: 사용자 정보 추출 시 NullPointerException이 발생한다
        assertThatThrownBy(() -> githubOAuth2Strategy.getUserInfo(oauth2User))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("GitHub 필수 속성이 누락된 경우 적절히 처리된다")
    void shouldHandleMissingGithubAttributes() {
        // Given: login 속성이 누락된 GitHub 사용자 정보가 주어진다
        Map<String, Object> attributes = Map.of(
                "email", "user@example.com",
                "avatar_url", "https://github.com/avatar.jpg",
                "name", "Test User"
        );
        
        given(oauth2User.getAttributes()).willReturn(attributes);

        // When & Then: 사용자 정보 추출 시 NullPointerException이 발생한다
        assertThatThrownBy(() -> githubOAuth2Strategy.getUserInfo(oauth2User))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("다양한 사용자 정보로 올바르게 동작한다")
    void shouldWorkWithVariousUserInfo() {
        // Given: 다양한 GitHub 사용자 정보가 준비되어 있다
        Map<String, Object> attributes1 = Map.of(
                "login", "john-doe",
                "email", "john.doe@company.com",
                "avatar_url", "avatar.jpg",
                "name", "John Doe"
        );
        Map<String, Object> attributes2 = Map.of(
                "login", "jane-smith",
                "email", "jane.smith@othercompany.org",
                "avatar_url", "avatar2.jpg",
                "name", "Jane Smith"
        );

        // When & Then: 첫 번째 이메일로 성공한다
        given(oauth2User.getAttributes()).willReturn(attributes1);
        MadpOAuth2UserInfo result1 = githubOAuth2Strategy.getUserInfo(oauth2User);
        assertThat(result1.mail()).isEqualTo("john.doe@company.com");

        // When & Then: 두 번째 이메일로도 성공한다
        given(oauth2User.getAttributes()).willReturn(attributes2);
        MadpOAuth2UserInfo result2 = githubOAuth2Strategy.getUserInfo(oauth2User);
        assertThat(result2.mail()).isEqualTo("jane.smith@othercompany.org");
    }
}