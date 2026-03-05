package madp.auth.global.infrastructure.internal;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import madp.auth.domain.domain.entity.OAuth2SessionEntity;
import madp.auth.domain.domain.repository.OAuth2SessionRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

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
        if(state == null || state.isEmpty()) return;
        OAuth2SessionEntity oAuth2SessionEntity = getSessionByState(state);
        if(oAuth2SessionEntity == null) return;
        addUserHeaders(template, oAuth2SessionEntity.getUserId().toString(), oAuth2SessionEntity.getUserRole());

        log.info("[InternalInterceptor] No user info found in headers or state");
    }
    
    private void addUserHeaders(RequestTemplate template, String userId, String userRole) {
        log.info("[InternalInterceptor] Adding headers - X-User-Id: {}, X-User-Role: {}", userId, userRole);
        template.header("X-User-Id", userId);
        template.header("X-User-Role", userRole);
    }
    
    private OAuth2SessionEntity getSessionByState(String state) {
        return oAuth2SessionRepository.findBySessionId(state).orElse(null);
    }
}