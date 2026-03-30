package madp.auth.domain.infrastructure.security.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import madp.auth.domain.domain.enums.OAuth2Type;
import madp.auth.domain.infrastructure.security.strategy.OAuth2Strategy;
import madp.auth.domain.infrastructure.security.vo.MadpOAuth2UserInfo;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class GithubOAuth2Strategy implements OAuth2Strategy {

    @Override
    public OAuth2Type getOAuth2ProviderType() {
        return OAuth2Type.GITHUB;
    }

    @Override
    public MadpOAuth2UserInfo getUserInfo(OAuth2User user) {
        Map<String, Object> attributes = user.getAttributes();
        String providerId = attributes.get("id").toString();
        String mail =  attributes.get("email").toString();
        String profile = attributes.get("avatar_url").toString();
        String nickname = attributes.get("login").toString();

        log.debug(providerId);
        log.debug(mail);
        log.debug(profile);
        log.debug(nickname);

        return new MadpOAuth2UserInfo(providerId, mail, profile, nickname, OAuth2Type.GITHUB);
    }
}