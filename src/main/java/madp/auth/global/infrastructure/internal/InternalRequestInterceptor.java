package madp.auth.global.infrastructure.internal;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Base64;

@Slf4j
@Component
public class InternalRequestInterceptor implements RequestInterceptor {
    
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
        if (state != null && !state.isEmpty()) {
            String[] userInfo = decodeUserInfo(state);
            if (isValidUserInfo(userInfo)) {
                log.info("[InternalInterceptor] Found OAuth2 state - userId: {}, userRole: {}", userInfo[0], userInfo[1]);
                addUserHeaders(template, userInfo[0], userInfo[1]);
                return;
            }
            log.warn("[InternalInterceptor] Invalid user info in state parameter");
        }

        log.info("[InternalInterceptor] No user info found in headers or state");
    }
    
    private void addUserHeaders(RequestTemplate template, String userId, String userRole) {
        log.info("[InternalInterceptor] Adding headers - X-User-Id: {}, X-User-Role: {}", userId, userRole);
        template.header("X-User-Id", userId);
        template.header("X-User-Role", userRole);
    }
    
    private String[] decodeUserInfo(String encodedState) {
        if (encodedState == null || encodedState.isEmpty()) {
            return null;
        }

        try {
            String decoded = new String(Base64.getDecoder().decode(encodedState));
            return decoded.split(":");
        }
        catch (IllegalArgumentException e) {
            log.warn("[InternalInterceptor] Invalid Base64 format: {}", e.getMessage());
            return null;
        }
    }
    
    private boolean isValidUserInfo(String[] userInfo) {
        return userInfo != null && userInfo.length == 2 && 
               userInfo[0] != null && !userInfo[0].isEmpty() &&
               userInfo[1] != null && !userInfo[1].isEmpty();
    }
}