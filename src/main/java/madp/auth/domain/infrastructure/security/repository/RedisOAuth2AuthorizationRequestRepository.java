package madp.auth.domain.infrastructure.security.repository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    
    private static final String OAUTH2_AUTHORIZATION_REQUEST_PREFIX = "oauth2_auth_request:";
    private static final String STATE_PARAM = "state";
    private static final int EXPIRATION_MINUTES = 10;
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    private RedisTemplate<String, OAuth2AuthorizationRequest> getJdkRedisTemplate() {
        RedisTemplate<String, OAuth2AuthorizationRequest> template = new RedisTemplate<>();
        template.setConnectionFactory(redisTemplate.getConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new JdkSerializationRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new JdkSerializationRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
    
    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        String state = getStateParameter(request);
        if (!StringUtils.hasText(state)) {
            return null;
        }
        
        String key = OAUTH2_AUTHORIZATION_REQUEST_PREFIX + state;
        OAuth2AuthorizationRequest authorizationRequest = getJdkRedisTemplate().opsForValue().get(key);
        log.debug("Loading authorization request for state: {}, found: {}", state, authorizationRequest != null);
        
        return authorizationRequest;
    }
    
    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            removeAuthorizationRequest(request, response);
            return;
        }
        
        String state = authorizationRequest.getState();
        if (!StringUtils.hasText(state)) {
            log.warn("Cannot save authorization request without state parameter");
            return;
        }
        
        String key = OAUTH2_AUTHORIZATION_REQUEST_PREFIX + state;
        getJdkRedisTemplate().opsForValue().set(key, authorizationRequest, EXPIRATION_MINUTES, TimeUnit.MINUTES);
        log.debug("Saved authorization request for state: {}", state);
    }
    
    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        String state = getStateParameter(request);
        if (!StringUtils.hasText(state)) {
            return null;
        }
        
        String key = OAUTH2_AUTHORIZATION_REQUEST_PREFIX + state;
        OAuth2AuthorizationRequest authorizationRequest = getJdkRedisTemplate().opsForValue().get(key);
        
        if (authorizationRequest != null) {
            getJdkRedisTemplate().delete(key);
            log.debug("Removed authorization request for state: {}", state);
        }
        
        return authorizationRequest;
    }
    
    private String getStateParameter(HttpServletRequest request) {
        return request.getParameter(STATE_PARAM);
    }
}