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
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, "/auth/oauth2/authorization"
        );
        this.oAuth2SessionRepository = oAuth2SessionRepository;
        this.oAuth2SessionProperties = oAuth2SessionProperties;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
        if (authorizationRequest == null) {
            return null;
        }

        return customizeAuthorizationRequest(authorizationRequest, request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
        if (authorizationRequest == null) {
            return null;
        }

        return customizeAuthorizationRequest(authorizationRequest, request);
    }

    private OAuth2AuthorizationRequest customizeAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        String userRole = request.getHeader("X-User-Role");

        log.info("[CustomResolver] Creating authorization request - userId: {}, userRole: {}", userId, userRole);

        // 사용자 정보가 없으면 기본 요청 반환
        if (userId == null || userRole == null) {
            return authorizationRequest;
        }

        // 세션 생성 및 저장
        String sessionId = createAndSaveSession(userId, userRole);
        
        // additionalParameters에 sessionId 추가
        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .additionalParameters(params -> params.put("session_id", sessionId))
                .build();
    }
    
    private String createAndSaveSession(String userId, String userRole) {
        String sessionId = UUID.randomUUID().toString();
        
        OAuth2SessionEntity sessionEntity = OAuth2SessionEntity.builder()
                .sessionId(sessionId)
                .userId(Long.parseLong(userId))
                .userRole(userRole)
                .expiration(oAuth2SessionProperties.getExpiration())
                .build();
                
        oAuth2SessionRepository.save(sessionEntity);
        log.info("[CustomResolver] Created session - sessionId: {}, userId: {}, userRole: {}", sessionId, userId, userRole);
        
        return sessionId;
    }
}