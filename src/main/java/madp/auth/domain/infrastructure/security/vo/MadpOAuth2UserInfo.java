package madp.auth.domain.infrastructure.security.vo;

import madp.auth.domain.domain.enums.OAuth2Type;

public record MadpOAuth2UserInfo(
    String providerId,
    String mail,
    String profile,
    String name,
    OAuth2Type oAuth2Type
) { }
