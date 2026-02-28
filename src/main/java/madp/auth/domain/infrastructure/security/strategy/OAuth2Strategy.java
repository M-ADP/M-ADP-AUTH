package madp.auth.domain.infrastructure.security.strategy;

import madp.auth.domain.domain.enums.OAuth2Type;
import madp.auth.domain.infrastructure.security.vo.MadpOAuth2UserInfo;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2Strategy {
    OAuth2Type getOAuth2ProviderType();

    MadpOAuth2UserInfo getUserInfo(OAuth2User user);
}
