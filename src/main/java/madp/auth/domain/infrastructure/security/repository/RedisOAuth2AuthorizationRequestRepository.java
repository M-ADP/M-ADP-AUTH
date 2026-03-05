package madp.auth.domain.infrastructure.security.repository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import madp.auth.domain.domain.entity.OAuth2SessionEntity;
import madp.auth.domain.domain.repository.OAuth2SessionRepository;
import madp.auth.global.properties.OAuth2SessionProperties;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    
    private final OAuth2SessionRepository oAuth2SessionRepository;
    private final OAuth2SessionProperties oAuth2SessionProperties;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        String state = request.getParameter("state");
        if (state == null) {
            return null;
        }
        
        return oAuth2SessionRepository.findBySessionId(state)
                .map(OAuth2SessionEntity::getOAuth2AuthorizationRequest)
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            log.debug("[RedisAuthRepo] Authorization request is null, skipping save");
            return;
        }

        String state = authorizationRequest.getState();
        String userId = request.getHeader("X-User-Id");
        String userRole = request.getHeader("X-User-Role");

        log.info("[RedisAuthRepo] Saving authorization request - state: {}, userId: {}, userRole: {}", state, userId, userRole);

        OAuth2SessionEntity sessionEntity = OAuth2SessionEntity.builder()
                .sessionId(state)
                .userId(userId != null ? Long.parseLong(userId) : null)
                .userRole(userRole)
                .oAuth2AuthorizationRequest(authorizationRequest)
                .expiration(oAuth2SessionProperties.getExpiration())
                .build();
        
        oAuth2SessionRepository.save(sessionEntity);
        log.info("[RedisAuthRepo] Saved OAuth2Session to Redis with state: {}, userId: {}, userRole: {}", state, userId, userRole);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        String state = request.getParameter("state");
        if (state == null) {
            return null;
        }

        log.info("[RedisAuthRepo] Removing authorization request for state: {}", state);
        
        OAuth2SessionEntity sessionEntity = oAuth2SessionRepository.findBySessionId(state).orElse(null);
        if (sessionEntity != null) {
            oAuth2SessionRepository.delete(sessionEntity);
            log.info("[RedisAuthRepo] Removed OAuth2Session from Redis for state: {}", state);
            return sessionEntity.getOAuth2AuthorizationRequest();
        }

        log.warn("[RedisAuthRepo] No OAuth2Session found for state: {}", state);
        return null;
    }
}