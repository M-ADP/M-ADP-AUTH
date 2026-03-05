package madp.auth.global.infrastructure.internal;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import madp.auth.domain.domain.entity.OAuth2SessionEntity;
import madp.auth.domain.domain.repository.OAuth2SessionRepository;
import madp.auth.domain.infrastructure.security.constants.OAuth2SessionConstants;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.util.WebUtils;

@Component
@RequiredArgsConstructor
public class InternalRequestInterceptor implements RequestInterceptor {
    private final OAuth2SessionRepository oAuth2SessionRepository;
    
    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(attributes == null) return;

        HttpServletRequest request = attributes.getRequest();

        String userId = request.getHeader("X-User-Id");
        String userRole = request.getHeader("X-User-Role");
        
        if (userId != null && userRole != null) {
            addUserHeaders(template, userId, userRole);
            return;
        }

        Cookie oauth2SessionCookie = WebUtils.getCookie(request, OAuth2SessionConstants.SESSION_COOKIE_NAME);
        if(oauth2SessionCookie == null) return;
        
        OAuth2SessionEntity oAuth2SessionEntity = oAuth2SessionRepository.findById(oauth2SessionCookie.getValue()).orElse(null);
        if(oAuth2SessionEntity == null) return;
        
        addUserHeaders(template, oAuth2SessionEntity.getUserId(), oAuth2SessionEntity.getUserRole());

        oAuth2SessionRepository.delete(oAuth2SessionEntity);
    }
    
    private void addUserHeaders(RequestTemplate template, String userId, String userRole) {
        template.header("X-User-Id", userId);
        template.header("X-User-Role", userRole);
    }
}