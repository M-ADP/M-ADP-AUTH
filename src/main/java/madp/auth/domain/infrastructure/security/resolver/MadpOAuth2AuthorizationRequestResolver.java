package madp.auth.domain.infrastructure.security.resolver;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Slf4j
@Component
public class MadpOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;

    public MadpOAuth2AuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/auth/oauth2/authorization");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
        return customizeAuthorizationRequest(authorizationRequest, request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
        return customizeAuthorizationRequest(authorizationRequest, request);
    }

    private OAuth2AuthorizationRequest customizeAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request) {
        log.info(request.toString());

        // OAuth2 인증 안 함
        if (authorizationRequest == null) {
            return null;
        }
        log.info(authorizationRequest.toString());

        String userId = request.getHeader("X-User-Id");
        String userRole = request.getHeader("X-User-Role");

        if (userId != null && userRole != null) {
            log.info("[OAuth2Resolver] Found headers - userId: {}, userRole: {}", userId, userRole);

            // State parameter에 사용자 정보 인코딩
            String encodedState = encodeUserInfo(userId, userRole);
            log.info("[OAuth2Resolver] Encoded state: {}", encodedState);

            // 기존 authorizationRequest를 복사하면서 state 추가
            return OAuth2AuthorizationRequest.from(authorizationRequest)
                    .state(encodedState)
                    .build();
        }

        log.info("[OAuth2Resolver] No headers found, returning original authorizationRequest");
        return authorizationRequest;
    }

    private String encodeUserInfo(String userId, String userRole) {
        String data = userId + ":" + userRole;
        String encoded = Base64.getEncoder().encodeToString(data.getBytes());
        log.info("[OAuth2Resolver] Encoding '{}' -> '{}'", data, encoded);
        return encoded;
    }
}