package madp.auth.domain.infrastructure.security.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import madp.auth.global.enums.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
public class MadpOAuth2User implements OAuth2User {

    private final Long userId;
    private final String username;
    private final Role role;
    private final Map<String, Object> attributes;
    
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(role.getValue()));
    }

    @NonNull
    @Override
    public String getName() {
        return username;
    }

    @Builder
    public MadpOAuth2User(Long userId, String username, Role role, Map<String, Object> attributes) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.attributes = attributes;
    }

}