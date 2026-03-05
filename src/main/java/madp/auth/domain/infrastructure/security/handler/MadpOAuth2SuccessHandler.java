package madp.auth.domain.infrastructure.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import madp.auth.domain.domain.entity.AuthCodeEntity;
import madp.auth.domain.domain.entity.TokenEntity;
import madp.auth.global.enums.Role;
import madp.auth.domain.domain.repository.AuthCodeRepository;
import madp.auth.domain.domain.repository.TokenRepository;
import madp.auth.domain.infrastructure.jwt.JwtManager;
import madp.auth.domain.infrastructure.security.vo.MadpOAuth2User;
import madp.auth.global.properties.AuthCodeProperties;
import madp.auth.global.properties.JwtProperties;
import madp.auth.global.properties.WebProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MadpOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final JwtManager jwtManager;
    private final AuthCodeProperties authCodeProperties;
    private final TokenRepository tokenRepository;
    private final AuthCodeRepository authCodeRepository;
    private final JwtProperties jwtProperties;
    private final WebProperties webProperties;

    @Override
    public void onAuthenticationSuccess(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Authentication authentication
    ) throws IOException {
        log.info("[OAuth2SuccessHandler] ===== OAUTH2 AUTHENTICATION SUCCESS =====");
        log.info("[OAuth2SuccessHandler] Request URI: {}", request.getRequestURI());
        log.info("[OAuth2SuccessHandler] Request URL: {}", request.getRequestURL());
        log.info("[OAuth2SuccessHandler] Query String: {}", request.getQueryString());
        log.info("[OAuth2SuccessHandler] Authentication: {}", authentication);
        log.info("[OAuth2SuccessHandler] Authentication Principal: {}", authentication.getPrincipal());
        
        MadpOAuth2User madpOAuth2User = (MadpOAuth2User) authentication.getPrincipal();
        if(madpOAuth2User == null) {
            log.error("[OAuth2SuccessHandler] MadpOAuth2User is null - redirecting to error page");
            redirectToErrorPage(response);
            return;
        }

        Long userId = madpOAuth2User.getUserId();
        Role role = madpOAuth2User.getRole();
        log.info("[OAuth2SuccessHandler] User ID: {}, Role: {}", userId, role);

        String accessToken = jwtManager.generateAccessToken(userId, role);
        String authCode = UUID.randomUUID().toString();
        log.info("[OAuth2SuccessHandler] Generated authCode: {}", authCode);
        createAuthCode(authCode, accessToken);

        String refreshToken = jwtManager.generateRefreshToken(userId, role);
        saveRefreshToken(refreshToken, userId);

        ResponseCookie refreshTokenCookie = jwtManager.createRefreshTokenCookie(refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        log.info("[OAuth2SuccessHandler] Redirecting to frontend callback");
        redirectToCallback(response, authCode);
        log.info("[OAuth2SuccessHandler] ===== SUCCESS HANDLER END =====");
    }

    private void createAuthCode(String authCode, String accessToken) {
        AuthCodeEntity authCodeEntity = AuthCodeEntity.builder()
                .authCode(authCode)
                .accessToken(accessToken)
                .timeToLive(authCodeProperties.getExpiration())
                .build();
        authCodeRepository.save(authCodeEntity);
    }

    private void saveRefreshToken(String refreshToken, Long userId) {
        TokenEntity tokenEntity = TokenEntity.builder()
                .token(refreshToken)
                .userId(userId)
                .expiration(jwtProperties.getRefreshExpiration())
                .build();
        tokenRepository.save(tokenEntity);
    }

    private void redirectToErrorPage(HttpServletResponse response) throws IOException {
        String errorUrl = webProperties.getFrontEndUrl() + "/oauth2/error#reason=invalid_user";
        response.sendRedirect(errorUrl);
    }

    private void redirectToCallback(HttpServletResponse response, String authCode) throws IOException {
        String frontendUrl = webProperties.getFrontEndUrl() + "/oauth2/callback#code=" + authCode;
        response.sendRedirect(frontendUrl);
    }
}
