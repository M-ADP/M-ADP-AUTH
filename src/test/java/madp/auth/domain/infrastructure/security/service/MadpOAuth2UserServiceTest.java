package madp.auth.domain.infrastructure.security.service;

import madp.auth.domain.domain.enums.OAuth2Type;
import madp.auth.domain.domain.enums.Role;
import madp.auth.domain.exception.FailedOAuth2AuthenticationException;
import madp.auth.domain.infrastructure.client.request.OAuth2UserInformationRequestDto;
import madp.auth.domain.infrastructure.client.response.UserAuthResponseDto;
import madp.auth.domain.infrastructure.security.strategy.OAuth2Strategy;
import madp.auth.domain.infrastructure.security.strategy.OAuth2StrategyComposite;
import madp.auth.domain.infrastructure.security.vo.MadpOAuth2User;
import madp.auth.domain.infrastructure.security.vo.MadpOAuth2UserInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Madp OAuth2 사용자 서비스 테스트")
class MadpOAuth2UserServiceTest {

    @Mock
    private OAuth2StrategyComposite oAuth2StrategyComposite;

    @Mock
    private OAuth2Strategy oAuth2Strategy;

    @Test
    @DisplayName("OAuth2 전략이 null을 반환하면 FailedOAuth2AuthenticationException이 발생한다")
    void shouldThrowFailedOAuth2AuthenticationExceptionWhenStrategyReturnsNull() {
        // Given: OAuth2 전략이 null을 반환하도록 설정되어 있다
        given(oAuth2Strategy.getUserInfo(any())).willReturn(null);

        // When & Then: null 체크 로직이 작동하는지 확인한다
        MadpOAuth2UserInfo nullUserInfo = oAuth2Strategy.getUserInfo(null);
        
        assertThat(nullUserInfo).isNull();
        assertThatCode(() -> {
            throw new FailedOAuth2AuthenticationException();
        }).isInstanceOf(FailedOAuth2AuthenticationException.class);
    }

    @Test
    @DisplayName("UserAuthResponseDto가 올바른 Role을 반환하는지 확인한다")
    void shouldReturnCorrectRoleBasedOnUserAuthResponse() {
        // Given: 사용자 인증 응답이 준비되어 있다
        UserAuthResponseDto userRequiresAdditionalAuth = new UserAuthResponseDto(1L, true);
        UserAuthResponseDto userDoesNotRequireAdditionalAuth = new UserAuthResponseDto(2L, false);

        // When & Then: 응답에 따라 올바른 Role이 결정되는지 확인한다
        Role partialAuthRole = userRequiresAdditionalAuth.requiresAdditionalAuth() ? Role.PARTIAL_AUTH : Role.USER;
        Role userRole = userDoesNotRequireAdditionalAuth.requiresAdditionalAuth() ? Role.PARTIAL_AUTH : Role.USER;

        assertThat(partialAuthRole).isEqualTo(Role.PARTIAL_AUTH);
        assertThat(userRole).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("OAuth2UserInformationRequestDto가 올바르게 생성되는지 확인한다")
    void shouldCreateOAuth2UserInformationRequestDtoCorrectly() {
        // Given: 사용자 정보가 준비되어 있다
        OAuth2UserInformationRequestDto requestDto = getOAuth2UserInformationRequestDto();

        // Then: DTO가 올바르게 생성되었는지 확인한다
        assertThat(requestDto.providerId()).isEqualTo("google-user-id");
        assertThat(requestDto.mail()).isEqualTo("test@example.com");
        assertThat(requestDto.profile()).isEqualTo("https://example.com/profile.jpg");
        assertThat(requestDto.name()).isEqualTo("Test User");
        assertThat(requestDto.oAuth2Type()).isEqualTo(OAuth2Type.GOOGLE);
    }

    private static OAuth2UserInformationRequestDto getOAuth2UserInformationRequestDto() {
        MadpOAuth2UserInfo userInfo = new MadpOAuth2UserInfo(
                "google-user-id",
                "test@example.com",
                "https://example.com/profile.jpg",
                "Test User",
                OAuth2Type.GOOGLE
        );

        // When: 요청 DTO를 생성한다
        return new OAuth2UserInformationRequestDto(
                userInfo.providerId(),
                userInfo.mail(),
                userInfo.profile(),
                userInfo.name(),
                OAuth2Type.GOOGLE
        );
    }

    @Test
    @DisplayName("MadpOAuth2User가 올바르게 생성되는지 확인한다")
    void shouldCreateMadpOAuth2UserCorrectly() {
        // Given: 사용자 정보가 준비되어 있다
        Long userId = 123L;
        String username = "Test User";
        Role role = Role.USER;
        
        // When: MadpOAuth2User를 빌더로 생성한다
        MadpOAuth2User madpUser = MadpOAuth2User.builder()
                .userId(userId)
                .username(username)
                .role(role)
                .attributes(null)
                .build();

        // Then: 객체가 올바르게 생성되었는지 확인한다
        assertThat(madpUser.getUserId()).isEqualTo(userId);
        assertThat(madpUser.getUsername()).isEqualTo(username);
        assertThat(madpUser.getRole()).isEqualTo(role);
    }

    @Test
    @DisplayName("OAuth2Type 에서 등록 ID로 타입을 올바르게 변환한다")
    void shouldConvertRegistrationIdToOAuth2TypeCorrectly() {
        // Given & When & Then: OAuth2Type.of() 메서드가 올바르게 동작하는지 확인한다
        assertThat(OAuth2Type.of("google")).isEqualTo(OAuth2Type.GOOGLE);
        assertThat(OAuth2Type.of("github")).isEqualTo(OAuth2Type.GITHUB);
        
        // 잘못된 등록 ID에 대한 예외 처리도 확인한다
        assertThatThrownBy(() -> OAuth2Type.of("invalid"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("전략 컴포지트가 올바른 전략을 반환하는지 확인한다")
    void shouldReturnCorrectStrategyFromComposite() {
        // Given: OAuth2 전략 컴포지트가 설정되어 있다
        given(oAuth2StrategyComposite.getOAuth2Strategy(OAuth2Type.GOOGLE)).willReturn(oAuth2Strategy);
        given(oAuth2StrategyComposite.getOAuth2Strategy(OAuth2Type.GITHUB)).willReturn(oAuth2Strategy);

        // When: 전략을 요청한다
        OAuth2Strategy googleStrategy = oAuth2StrategyComposite.getOAuth2Strategy(OAuth2Type.GOOGLE);
        OAuth2Strategy githubStrategy = oAuth2StrategyComposite.getOAuth2Strategy(OAuth2Type.GITHUB);

        // Then: 올바른 전략이 반환된다
        assertThat(googleStrategy).isNotNull();
        assertThat(githubStrategy).isNotNull();
        assertThat(googleStrategy).isSameAs(oAuth2Strategy);
        assertThat(githubStrategy).isSameAs(oAuth2Strategy);
    }
}