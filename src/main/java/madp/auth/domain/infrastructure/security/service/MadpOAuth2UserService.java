package madp.auth.domain.infrastructure.security.service;

import lombok.RequiredArgsConstructor;
import madp.auth.domain.domain.enums.OAuth2Type;
import madp.auth.global.enums.Role;
import madp.auth.domain.exception.FailedOAuth2AuthenticationException;
import madp.auth.domain.infrastructure.client.UserClient;
import madp.auth.domain.infrastructure.client.request.OAuth2UserInformationRequestDto;
import madp.auth.domain.infrastructure.client.response.UserAuthResponseDto;
import madp.auth.domain.infrastructure.security.strategy.OAuth2StrategyComposite;
import madp.auth.domain.infrastructure.security.vo.MadpOAuth2User;
import madp.auth.domain.infrastructure.security.vo.MadpOAuth2UserInfo;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MadpOAuth2UserService extends DefaultOAuth2UserService {
    private final OAuth2StrategyComposite oAuth2StrategyComposite;
    private final UserClient userClient;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        OAuth2Type oAuth2Type = OAuth2Type.of(userRequest.getClientRegistration().getRegistrationId());
        MadpOAuth2UserInfo madpOAuth2UserInfo = oAuth2StrategyComposite.getOAuth2Strategy(oAuth2Type).getUserInfo(oauth2User);

        if(madpOAuth2UserInfo == null) throw new FailedOAuth2AuthenticationException();
        
        OAuth2UserInformationRequestDto oAuth2UserInformationRequestDto = new OAuth2UserInformationRequestDto(madpOAuth2UserInfo.providerId(), madpOAuth2UserInfo.mail(), madpOAuth2UserInfo.profile(), madpOAuth2UserInfo.name(), oAuth2Type);
        UserAuthResponseDto userAuthResponseDto = userClient.getUserAuthStatus(oAuth2UserInformationRequestDto);

        return MadpOAuth2User.builder()
                .userId(userAuthResponseDto.userId())
                .username(oAuth2UserInformationRequestDto.name())
                .role(userAuthResponseDto.requiresAdditionalAuth() ? Role.PARTIAL_AUTH : Role.USER)
                .attributes(oauth2User.getAttributes())
                .build();
    }
}
