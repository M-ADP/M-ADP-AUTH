package madp.auth.domain.infrastructure.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import madp.auth.global.properties.WebProperties;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
        String errorMessage = URLEncoder.encode(exception.getClass().getSimpleName(), StandardCharsets.UTF_8);
        
        String frontendUrl = webProperties.getFrontEndUrl() + "/oauth2/callback#error=" + errorMessage;
        response.sendRedirect(frontendUrl);
    }
}
