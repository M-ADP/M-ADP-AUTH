package madp.auth.global.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.io.IOException;
import java.util.Objects;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Madp 사용자 정보 추출 필터 테스트")
class MadpUserInfoExtractorFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private MadpUserInfoExtractorFilter filter;

    @BeforeEach
    void setUp() {
        PathMatcher pathMatcher = new AntPathMatcher();
        String[] excludedPaths = new String[]{"/auth/reissue", "/auth/code", "/oauth2/callback/**", "/oauth2/authorization/google", "/actuator/health"};
        filter = new MadpUserInfoExtractorFilter(pathMatcher, excludedPaths);
        
        // SecurityContext 초기화
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 사용자 헤더가 있을 때 SecurityContext에 인증 정보를 설정한다")
    void shouldSetAuthenticationInSecurityContextWithValidHeaders() throws ServletException, IOException {
        // Given: 유효한 사용자 헤더가 요청에 포함되어 있다
        given(request.getHeader("X-User-Id")).willReturn("123");
        given(request.getHeader("X-User-Role")).willReturn("ROLE_USER");

        // When: 필터를 실행한다
        filter.doFilterInternal(request, response, filterChain);

        // Then: SecurityContext에 인증 정보가 설정된다
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(Objects.requireNonNull(authentication.getPrincipal())).isEqualTo(123L);
        assertThat(authentication.getAuthorities()).hasSize(1);
        assertThat(authentication.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
        
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("PARTIAL_AUTH 권한이 올바르게 설정된다")
    void shouldSetPartialAuthRoleCorrectly() throws ServletException, IOException {
        // Given: PARTIAL_AUTH 권한을 가진 사용자 헤더가 요청에 포함되어 있다
        given(request.getHeader("X-User-Id")).willReturn("456");
        given(request.getHeader("X-User-Role")).willReturn("ROLE_PARTIAL_AUTH");

        // When: 필터를 실행한다
        filter.doFilterInternal(request, response, filterChain);

        // Then: SecurityContext에 PARTIAL_AUTH 권한이 설정된다
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(Objects.requireNonNull(authentication.getPrincipal())).isEqualTo(456L);
        assertThat(authentication.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_PARTIAL_AUTH");
        
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("X-User-Id 헤더가 없으면 인증 정보를 설정하지 않는다")
    void shouldNotSetAuthenticationWhenUserIdHeaderMissing() throws ServletException, IOException {
        // Given: X-User-Id 헤더가 없다
        given(request.getHeader("X-User-Id")).willReturn(null);
        given(request.getHeader("X-User-Role")).willReturn("USER");

        // When: 필터를 실행한다
        filter.doFilterInternal(request, response, filterChain);

        // Then: SecurityContext에 인증 정보가 설정되지 않는다
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();
        
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("X-Role 헤더가 없으면 인증 정보를 설정하지 않는다")
    void shouldNotSetAuthenticationWhenRoleHeaderMissing() throws ServletException, IOException {
        // Given: X-Role 헤더가 없다
        given(request.getHeader("X-User-Id")).willReturn("123");
        given(request.getHeader("X-User-Role")).willReturn(null);

        // When: 필터를 실행한다
        filter.doFilterInternal(request, response, filterChain);

        // Then: SecurityContext에 인증 정보가 설정되지 않는다
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();
        
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("두 헤더가 모두 없으면 인증 정보를 설정하지 않는다")
    void shouldNotSetAuthenticationWhenBothHeadersMissing() throws ServletException, IOException {
        // Given: 두 헤더가 모두 없다
        given(request.getHeader("X-User-Id")).willReturn(null);
        given(request.getHeader("X-User-Role")).willReturn(null);

        // When: 필터를 실행한다
        filter.doFilterInternal(request, response, filterChain);

        // Then: SecurityContext에 인증 정보가 설정되지 않는다
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();
        
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("제외된 경로는 필터링하지 않는다 - /auth/reissue")
    void shouldNotFilterExcludedPath_AuthReissue() {
        // Given: 제외된 경로 요청이 준비되어 있다
        given(request.getRequestURI()).willReturn("/auth/reissue");

        // When: shouldNotFilter를 확인한다
        boolean result = filter.shouldNotFilter(request);

        // Then: 필터링하지 않는다
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("제외된 경로는 필터링하지 않는다 - /auth/code")
    void shouldNotFilterExcludedPath_AuthCode() {
        // Given: 제외된 경로 요청이 준비되어 있다
        given(request.getRequestURI()).willReturn("/auth/code");

        // When: shouldNotFilter를 확인한다
        boolean result = filter.shouldNotFilter(request);

        // Then: 필터링하지 않는다
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("제외된 경로는 필터링하지 않는다 - /oauth2/callback/** 패턴")
    void shouldNotFilterExcludedPath_OAuth2Callback() {
        // Given: OAuth2 콜백 경로 요청이 준비되어 있다
        given(request.getRequestURI()).willReturn("/oauth2/callback/google");

        // When: shouldNotFilter를 확인한다
        boolean result = filter.shouldNotFilter(request);

        // Then: 필터링하지 않는다
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("제외된 경로는 필터링하지 않는다 - /actuator/health")
    void shouldNotFilterExcludedPath_ActuatorHealth() {
        // Given: 액추에이터 헬스 경로 요청이 준비되어 있다
        given(request.getRequestURI()).willReturn("/actuator/health");

        // When: shouldNotFilter를 확인한다
        boolean result = filter.shouldNotFilter(request);

        // Then: 필터링하지 않는다
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("제외되지 않은 경로는 필터링한다")
    void shouldFilterNonExcludedPath() {
        // Given: 제외되지 않은 경로 요청이 준비되어 있다
        given(request.getRequestURI()).willReturn("/api/some-endpoint");

        // When: shouldNotFilter를 확인한다
        boolean result = filter.shouldNotFilter(request);

        // Then: 필터링한다
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("와일드카드 패턴이 올바르게 매칭된다")
    void shouldMatchWildcardPatternCorrectly() {
        // Given: 와일드카드 패턴에 매칭되는 여러 경로가 준비되어 있다
        given(request.getRequestURI()).willReturn("/oauth2/callback/github");
        boolean result1 = filter.shouldNotFilter(request);

        given(request.getRequestURI()).willReturn("/oauth2/callback/google/success");
        boolean result2 = filter.shouldNotFilter(request);

        // When & Then: 모든 경로가 매칭된다
        assertThat(result1).isTrue();
        assertThat(result2).isTrue();
    }

    @Test
    @DisplayName("숫자 형식이 아닌 User-Id 헤더는 예외를 발생시킨다")
    void shouldThrowExceptionWhenUserIdIsNotNumeric() {
        // Given: 숫자가 아닌 User-Id 헤더가 주어진다
        given(request.getHeader("X-User-Id")).willReturn("not-a-number");
        given(request.getHeader("X-User-Role")).willReturn("USER");

        // When & Then: NumberFormatException이 발생한다
        assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    @DisplayName("유효하지 않은 Role 헤더는 예외를 발생시킨다")
    void shouldThrowExceptionWhenRoleIsInvalid() {
        // Given: 유효하지 않은 Role 헤더가 주어진다
        given(request.getHeader("X-User-Id")).willReturn("123");
        given(request.getHeader("X-User-Role")).willReturn("INVALID_ROLE");

        // When & Then: 예외가 발생한다
        assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("다양한 사용자 ID로 올바르게 동작한다")
    void shouldWorkWithVariousUserIds() throws ServletException, IOException {
        // Given: 다양한 사용자 ID가 준비되어 있다
        Long[] userIds = {1L, 999L, 1000000L};
        
        for (Long userId : userIds) {
            // 새로운 SecurityContext로 초기화
            SecurityContextHolder.clearContext();
            
            given(request.getHeader("X-User-Id")).willReturn(userId.toString());
            given(request.getHeader("X-User-Role")).willReturn("ROLE_USER");

            // When: 필터를 실행한다
            filter.doFilterInternal(request, response, filterChain);

            // Then: 각 사용자 ID가 올바르게 설정된다
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Assertions.assertNotNull(authentication);
            assertThat(Objects.requireNonNull(authentication.getPrincipal())).isEqualTo(userId);
        }
        
        verify(filterChain, times(userIds.length)).doFilter(request, response);
    }
}
