package madp.auth.global.infrastructure.internal;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import madp.auth.domain.domain.entity.OAuth2SessionEntity;
import madp.auth.domain.domain.repository.OAuth2SessionRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

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
        String userId = request.getHeader("X-User-Id");
        String userRole = request.getHeader("X-User-Role");
        
        log.info("[InternalInterceptor] X-User-Id header: {}, X-User-Role header: {}", userId, userRole);
        
        if (userId != null && userRole != null) {
            log.info("[InternalInterceptor] Using headers for user info");
            addUserHeaders(template, userId, userRole);
            return;
        }

        // OAuth2 콜백에서 state parameter 확인
        String state = request.getParameter("state");
        log.info("[InternalInterceptor] state parameter: {}", state);
        log.info("[InternalInterceptor] Request URI: {}", request.getRequestURI());
        log.info("[InternalInterceptor] Request URL: {}", request.getRequestURL());
        log.info("[InternalInterceptor] All parameters: {}", request.getParameterMap().keySet());

        if (state != null) {
            log.info("[InternalInterceptor] Searching OAuth2Session by state: {}", state);
            Optional<OAuth2SessionEntity> sessionOpt = oAuth2SessionRepository.findBySessionId(state);
            log.info("[InternalInterceptor] OAuth2Session found: {}", sessionOpt.isPresent());
            if (sessionOpt.isPresent()) {
                OAuth2SessionEntity oAuth2Session = sessionOpt.get();
                log.info("[InternalInterceptor] Session userId: {}, userRole: {}", oAuth2Session.getUserId(), oAuth2Session.getUserRole());
                addUserHeaders(template, oAuth2Session.getUserId().toString(), oAuth2Session.getUserRole());
            } else {
                log.warn("[InternalInterceptor] No OAuth2Session found for state: {} - userId will be null", state);
            }
        } else {
            log.info("[InternalInterceptor] No state parameter and no X-User-Id/X-User-Role headers - skipping user info");
        }
    }
    
    private void addUserHeaders(RequestTemplate template, String userId, String userRole) {
        log.info("[InternalInterceptor] Adding headers - X-User-Id: {}, X-User-Role: {}", userId, userRole);
        template.header("X-User-Id", userId);
        template.header("X-User-Role", userRole);
    }
    
}