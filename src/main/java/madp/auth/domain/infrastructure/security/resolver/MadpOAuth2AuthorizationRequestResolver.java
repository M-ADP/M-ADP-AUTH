package madp.auth.domain.infrastructure.security.resolver;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import madp.auth.domain.domain.entity.OAuth2SessionEntity;
import madp.auth.domain.domain.repository.OAuth2SessionRepository;
import madp.auth.domain.infrastructure.security.constants.OAuth2SessionConstants;
import madp.auth.global.properties.OAuth2SessionProperties;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Slf4j
@Component
public class MadpOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;
    private final OAuth2SessionRepository oauth2SessionRepository;
    private final OAuth2SessionProperties oAuth2SessionProperties;

    public MadpOAuth2AuthorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2SessionRepository oauth2SessionRepository,
            OAuth2SessionProperties oAuth2SessionProperties) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
            clientRegistrationRepository, "/auth/oauth2/authorization");
        this.oauth2SessionRepository = oauth2SessionRepository;
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
        
        if (authorizationRequest == null) {
            return null;
        }

        // 원본 헤더 정보 저장
        String sessionKey = UUID.randomUUID().toString();
        String userId = request.getHeader("X-User-Id");
        String userRole = request.getHeader("X-User-Role");

        if (userId != null && userRole != null) {
            OAuth2SessionEntity sessionEntity = OAuth2SessionEntity.builder()
                    .sessionKey(sessionKey)
                    .userId(userId)
                    .userRole(userRole)
                    .timeToLive(600L) // 10분
                    .build();

            oauth2SessionRepository.save(sessionEntity);

            log.info(sessionKey);
            log.info(userId);

            // 쿠키에 세션 키 저장
            setCookie(sessionKey);
        }

        return authorizationRequest;
    }

    private void setCookie(String sessionKey) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletResponse response = attrs.getResponse();

        if (response != null) {
            Cookie sessionCookie = createSessionCookie(sessionKey);
            response.addCookie(sessionCookie);
        }
    }

    private Cookie createSessionCookie(String sessionKey) {
        Cookie cookie = new Cookie(OAuth2SessionConstants.SESSION_COOKIE_NAME, sessionKey);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(oAuth2SessionProperties.getExpiration());
        return cookie;
    }
}