package madp.auth.domain.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import madp.auth.domain.application.service.AuthService;
import madp.auth.domain.exception.AuthCodeNotFoundException;
import madp.auth.domain.infrastructure.jwt.constants.JwtConstants;
import madp.auth.domain.presentation.dto.request.AuthCodeRequestDto;
import madp.auth.domain.presentation.dto.response.TokenResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.servlet.MockMvc;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import madp.auth.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auth 컨트롤러 테스트")
class AuthControllerTest {

    @Mock
    private AuthService authService;
    
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
        @DisplayName("POST /auth/reissue - 유효한 리프레시 토큰으로 재발급 성공")
    void shouldReissueTokenSuccessfullyWithValidRefreshToken() throws Exception {
        // Given: 유효한 리프레시 토큰과 새로운 토큰 정보가 준비되어 있다
        String refreshToken = "valid-refresh-token";
        String newAccessToken = "new-access-token";
        ResponseCookie newRefreshCookie = ResponseCookie.from(JwtConstants.REFRESH_TOKEN_COOKIE_HEADER, "new-refresh-token")
                .maxAge(86400)
                .build();
        TokenResponseDto tokenResponse = TokenResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshTokenCookie(newRefreshCookie)
                .build();
        
        given(authService.reissueToken(refreshToken)).willReturn(tokenResponse);

        // When & Then: 토큰 재발급 요청이 성공한다
        mockMvc.perform(post("/auth/reissue")
                        .cookie(new jakarta.servlet.http.Cookie(JwtConstants.REFRESH_TOKEN_COOKIE_HEADER, refreshToken))
                        )
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refresh_token=new-refresh-token")))
                .andExpect(jsonPath("$.access_token").value(newAccessToken));

        verify(authService).reissueToken(refreshToken);
    }

    @Test
        @DisplayName("POST /auth/reissue - 리프레시 토큰이 없으면 RefreshTokenNotFoundException 발생")
    void shouldThrowRefreshTokenNotFoundExceptionWhenRefreshTokenMissing() throws Exception {
        // When & Then: 리프레시 토큰 없이 재발급 요청 시 예외가 발생한다
        mockMvc.perform(post("/auth/reissue")
                        )
                .andExpect(status().isNotFound());

        verify(authService, never()).reissueToken(any());
    }

    @Test
        @DisplayName("POST /auth/logout - 유효한 리프레시 토큰으로 로그아웃 성공")
    void shouldLogoutSuccessfullyWithValidRefreshToken() throws Exception {
        // Given: 유효한 리프레시 토큰과 삭제된 쿠키가 준비되어 있다
        String refreshToken = "valid-refresh-token";
        ResponseCookie deletedCookie = ResponseCookie.from(JwtConstants.REFRESH_TOKEN_COOKIE_HEADER, "")
                .maxAge(0)
                .build();
        
        given(authService.deleteRefreshToken(refreshToken)).willReturn(deletedCookie);

        // When & Then: 로그아웃 요청이 성공한다
        mockMvc.perform(post("/auth/logout")
                        .cookie(new jakarta.servlet.http.Cookie(JwtConstants.REFRESH_TOKEN_COOKIE_HEADER, refreshToken))
                        )
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refresh_token=; Max-Age=0")));

        verify(authService).deleteRefreshToken(refreshToken);
    }

    @Test
        @DisplayName("POST /auth/logout - 리프레시 토큰이 없으면 RefreshTokenNotFoundException 발생")
    void shouldThrowRefreshTokenNotFoundExceptionWhenRefreshTokenMissingForLogout() throws Exception {
        // When & Then: 리프레시 토큰 없이 로그아웃 요청 시 예외가 발생한다
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isNotFound());

        verify(authService, never()).deleteRefreshToken(any());
    }

    @Test
        @DisplayName("POST /auth/code - 유효한 인증 코드로 액세스 토큰 조회 성공")
    void shouldGetAccessTokenSuccessfullyWithValidAuthCode() throws Exception {
        // Given: 유효한 인증 코드와 액세스 토큰이 준비되어 있다
        String authCode = "valid-auth-code";
        String accessToken = "valid-access-token";
        AuthCodeRequestDto request = new AuthCodeRequestDto(authCode);
        
        given(authService.getAccessTokenByAuthCode(authCode)).willReturn(accessToken);

        // When & Then: 인증 코드로 액세스 토큰 조회가 성공한다
        mockMvc.perform(post("/auth/code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value(accessToken));

        verify(authService).getAccessTokenByAuthCode(authCode);
    }

    @Test
        @DisplayName("POST /auth/code - 유효하지 않은 인증 코드로 요청 시 AuthCodeNotFoundException 발생")
    void shouldThrowAuthCodeNotFoundExceptionWithInvalidAuthCode() throws Exception {
        // Given: 유효하지 않은 인증 코드가 주어진다
        String invalidAuthCode = "invalid-auth-code";
        AuthCodeRequestDto request = new AuthCodeRequestDto(invalidAuthCode);
        
        given(authService.getAccessTokenByAuthCode(invalidAuthCode))
                .willThrow(new AuthCodeNotFoundException());

        // When & Then: 유효하지 않은 인증 코드로 요청 시 예외가 발생한다
        mockMvc.perform(post("/auth/code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());

        verify(authService).getAccessTokenByAuthCode(invalidAuthCode);
    }

    @Test
        @DisplayName("POST /auth/code - 빈 인증 코드로 요청 시 유효성 검증 실패")
    void shouldFailValidationWithEmptyAuthCode() throws Exception {
        // Given: 빈 인증 코드가 주어진다
        AuthCodeRequestDto request = new AuthCodeRequestDto("");

        // When & Then: 빈 인증 코드로 요청 시 유효성 검증이 실패한다
        mockMvc.perform(post("/auth/code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        )
                .andExpect(status().isBadRequest());

        verify(authService, never()).getAccessTokenByAuthCode(any());
    }

    @Test
        @DisplayName("POST /auth/code - null 인증 코드로 요청 시 유효성 검증 실패")
    void shouldFailValidationWithNullAuthCode() throws Exception {
        // Given: null 인증 코드가 주어진다
        AuthCodeRequestDto request = new AuthCodeRequestDto(null);

        // When & Then: null 인증 코드로 요청 시 유효성 검증이 실패한다
        mockMvc.perform(post("/auth/code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        )
                .andExpect(status().isBadRequest());

        verify(authService, never()).getAccessTokenByAuthCode(any());
    }

    @Test
        @DisplayName("POST /auth/code - 잘못된 JSON 형식으로 요청 시 400 에러 발생")
    void shouldReturn400WithInvalidJsonFormat() throws Exception {
        // When & Then: 잘못된 JSON 형식으로 요청 시 400 에러가 발생한다
        mockMvc.perform(post("/auth/code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid-json}")
                        )
                .andExpect(status().isBadRequest());

        verify(authService, never()).getAccessTokenByAuthCode(any());
    }
}
