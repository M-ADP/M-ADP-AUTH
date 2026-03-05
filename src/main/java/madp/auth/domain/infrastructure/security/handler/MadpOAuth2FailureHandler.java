package madp.auth.domain.infrastructure.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import madp.auth.global.properties.WebProperties;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class MadpOAuth2FailureHandler implements AuthenticationFailureHandler {
    private final WebProperties webProperties;

    @Override
    public void onAuthenticationFailure(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull AuthenticationException exception
    ) throws IOException {
        log.error("[OAuth2FailureHandler] ===== OAUTH2 AUTHENTICATION FAILURE =====");
        log.error("[OAuth2FailureHandler] Request URI: {}", request.getRequestURI());
        log.error("[OAuth2FailureHandler] Request URL: {}", request.getRequestURL());
        log.error("[OAuth2FailureHandler] Query String: {}", request.getQueryString());
        log.error("[OAuth2FailureHandler] Exception class: {}", exception.getClass().getSimpleName());
        log.error("[OAuth2FailureHandler] Exception message: {}", exception.getMessage());
        log.error("[OAuth2FailureHandler] Exception cause: {}", exception.getCause() != null ? exception.getCause().getMessage() : "null");
        log.error("[OAuth2FailureHandler] Full exception: ", exception);
        
        String errorMessage = URLEncoder.encode(exception.getClass().getSimpleName(), StandardCharsets.UTF_8);
        String frontendUrl = webProperties.getFrontEndUrl() + "/oauth2/callback#error=" + errorMessage;
        
        log.error("[OAuth2FailureHandler] Redirecting to: {}", frontendUrl);
        response.sendRedirect(frontendUrl);
        log.error("[OAuth2FailureHandler] ===== FAILURE HANDLER END =====");
    }
}
