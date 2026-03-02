package madp.auth.domain.infrastructure.client.request;

import madp.auth.domain.domain.enums.OAuth2Type;
import madp.auth.domain.exception.InvalidOAuth2UserInformationException;

public record OAuth2UserInformationRequestDto(
        String providerId,
        String mail,
        String profile,
        String name,
        OAuth2Type oAuth2Type
) {
    public OAuth2UserInformationRequestDto {
        if(providerId == null || providerId.isEmpty())
            throw new InvalidOAuth2UserInformationException("생성자 아이디는 필수값입니다");
        
        if(mail == null || mail.isEmpty())
            throw new InvalidOAuth2UserInformationException("이메일은 필수값입니다");
        
        if(profile == null || profile.isEmpty())
            throw new InvalidOAuth2UserInformationException("프로필은 필수값입니다");
        
        if(name == null || name.isEmpty())
            throw new InvalidOAuth2UserInformationException("이름은 필수값입니다");
        
        if(oAuth2Type == null)
            throw new InvalidOAuth2UserInformationException("OAuth2 타입은 필수값입니다");
    }
}
