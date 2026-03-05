package madp.auth.global.infrastructure.internal;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import madp.auth.domain.domain.entity.OAuth2SessionEntity;
import madp.auth.domain.domain.repository.OAuth2SessionRepository;
import madp.auth.domain.infrastructure.security.constants.OAuth2SessionConstants;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.util.WebUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class InternalRequestInterceptor implements RequestInterceptor {
    private final OAuth2SessionRepository oAuth2SessionRepository;
    
    @Override
    public void apply(RequestTemplate template) {
        log.info("[InternalInterceptor] apply() called");
        
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(attributes == null) {
            log.info("[InternalInterceptor] ServletRequestAttributes is null");
            return;
        }

        HttpServletRequest request = attributes.getRequest();
        log.info("[InternalInterceptor] HttpServletRequest: {}", request);

        String userId = request.getHeader("X-User-Id");
        String userRole = request.getHeader("X-User-Role");
        
        log.info("[InternalInterceptor] X-User-Id header: {}", userId);
        log.info("[InternalInterceptor] X-User-Role header: {}", userRole);
        
        if (userId != null && userRole != null) {
            log.info("[InternalInterceptor] Using headers for user info");
            addUserHeaders(template, userId, userRole);
            return;
        }

        log.info("[InternalInterceptor] No headers found, checking for OAuth2 session cookie");
        Cookie oauth2SessionCookie = WebUtils.getCookie(request, OAuth2SessionConstants.SESSION_COOKIE_NAME);
        
        if(oauth2SessionCookie == null) {
            log.info("[InternalInterceptor] No OAuth2 session cookie found");
            return;
        }
        
        log.info("[InternalInterceptor] Found OAuth2 session cookie: name={}, value={}", 
                oauth2SessionCookie.getName(), oauth2SessionCookie.getValue());
        
        OAuth2SessionEntity oAuth2SessionEntity = oAuth2SessionRepository.findById(oauth2SessionCookie.getValue()).orElse(null);
        
        if(oAuth2SessionEntity == null) {
            log.info("[InternalInterceptor] No OAuth2 session entity found in Redis for key: {}", oauth2SessionCookie.getValue());
            return;
        }
        
        log.info("[InternalInterceptor] Found OAuth2 session entity: userId={}, userRole={}", 
                oAuth2SessionEntity.getUserId(), oAuth2SessionEntity.getUserRole());
        
        addUserHeaders(template, oAuth2SessionEntity.getUserId(), oAuth2SessionEntity.getUserRole());

        log.info("[InternalInterceptor] Deleting OAuth2 session entity from Redis");
        oAuth2SessionRepository.delete(oAuth2SessionEntity);
    }
    
    private void addUserHeaders(RequestTemplate template, String userId, String userRole) {
        log.info("[InternalInterceptor] Adding headers - X-User-Id: {}, X-User-Role: {}", userId, userRole);
        template.header("X-User-Id", userId);
        template.header("X-User-Role", userRole);
    }
}