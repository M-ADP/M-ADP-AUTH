package madp.auth.domain.infrastructure.security.strategy.impl;

import lombok.RequiredArgsConstructor;
import madp.auth.domain.domain.enums.OAuth2Type;
import madp.auth.domain.domain.repository.TestUserRepository;
import madp.auth.domain.exception.UnsupportedAccountException;
import madp.auth.domain.infrastructure.security.strategy.OAuth2Strategy;
import madp.auth.domain.infrastructure.security.vo.MadpOAuth2UserInfo;
import madp.auth.global.properties.EmailProperties;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GoogleOAuth2Strategy implements OAuth2Strategy {
    private final EmailProperties emailProperties;
    private final TestUserRepository testUserRepository;

    @Override
    public OAuth2Type getOAuth2ProviderType() {
        return OAuth2Type.GOOGLE;
    }

    @Override
    public MadpOAuth2UserInfo getUserInfo(OAuth2User user) {
        Map<String, Object> attributes = user.getAttributes();
        String providerId = String.valueOf(attributes.get("sub"));
        String mail = String.valueOf(attributes.get("email"));
        String profile = String.valueOf(attributes.get("picture"));
        String name = String.valueOf(attributes.get("name"));

        if (mail == null || !testUserRepository.existsById(mail)) {
            throw new UnsupportedAccountException(emailProperties.getAllowedDomain());
        }

        return new MadpOAuth2UserInfo(providerId, mail, profile, name, OAuth2Type.GOOGLE);
    }
}
