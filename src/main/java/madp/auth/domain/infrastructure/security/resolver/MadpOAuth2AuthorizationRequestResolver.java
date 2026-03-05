package madp.auth.domain.infrastructure.security.resolver;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import madp.auth.domain.domain.entity.OAuth2SessionEntity;
import madp.auth.domain.domain.repository.OAuth2SessionRepository;
import madp.auth.global.properties.OAuth2SessionProperties;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class MadpOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;
    private final OAuth2SessionRepository oAuth2SessionRepository;
    private final OAuth2SessionProperties oAuth2SessionProperties;

    public MadpOAuth2AuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository, OAuth2SessionRepository oAuth2SessionRepository, OAuth2SessionProperties oAuth2SessionProperties) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/auth/oauth2/authorization");
        this.oAuth2SessionRepository = oAuth2SessionRepository;
        this.oAuth2SessionProperties = oAuth2SessionProperties;
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

            // Redis에 UUID 키로 사용자 정보 저장
            String stateId = getStateId(userId, userRole);
            log.info("[OAuth2Resolver] Saved user info to Redis with state ID: {}", stateId);

            // 기존 authorizationRequest를 복사하면서 state 추가
            return OAuth2AuthorizationRequest.from(authorizationRequest)
                    .state(stateId)
                    .build();
        }

        log.info("[OAuth2Resolver] No headers found, returning original authorizationRequest");
        return authorizationRequest;
    }

    private String getStateId(String userId, String userRole) {
        String stateId = UUID.randomUUID().toString();
        OAuth2SessionEntity oAuth2SessionEntity = OAuth2SessionEntity.builder()
                .sessionId(stateId)
                .userId(Long.parseLong(userId))
                .userRole(userRole)
                .expiration(oAuth2SessionProperties.getExpiration())
                .build();
        oAuth2SessionRepository.save(oAuth2SessionEntity);
        return stateId;
    }
}