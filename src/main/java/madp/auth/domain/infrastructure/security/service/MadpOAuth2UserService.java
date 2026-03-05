package madp.auth.domain.infrastructure.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class MadpOAuth2UserService extends DefaultOAuth2UserService {
    private final OAuth2StrategyComposite oAuth2StrategyComposite;
    private final UserClient userClient;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("[OAuth2UserService] ===== loadUser START =====");
        log.info("[OAuth2UserService] userRequest: {}", userRequest);
        log.info("[OAuth2UserService] clientRegistration: {}", userRequest.getClientRegistration());
        log.info("[OAuth2UserService] additionalParameters: {}", userRequest.getAdditionalParameters());
        
        // State parameter 확인 NPE 방지
        String state = (String) userRequest.getAdditionalParameters().get("state");
        log.info("[OAuth2UserService] State parameter: '{}'", state);
        
        OAuth2User oauth2User = super.loadUser(userRequest);
        log.info("[OAuth2UserService] OAuth2User loaded: {}", oauth2User.getName());
        log.info("[OAuth2UserService] OAuth2User attributes: {}", oauth2User.getAttributes());
        
        OAuth2Type oAuth2Type = OAuth2Type.of(userRequest.getClientRegistration().getRegistrationId());
        log.info("[OAuth2UserService] OAuth2Type: {}", oAuth2Type);
        
        MadpOAuth2UserInfo madpOAuth2UserInfo = oAuth2StrategyComposite.getOAuth2Strategy(oAuth2Type).getUserInfo(oauth2User);
        log.info("[OAuth2UserService] MadpOAuth2UserInfo: {}", madpOAuth2UserInfo);

        if(madpOAuth2UserInfo == null) {
            log.error("[OAuth2UserService] MadpOAuth2UserInfo is null - throwing FailedOAuth2AuthenticationException");
            throw new FailedOAuth2AuthenticationException();
        }
        
        // 기존 로직 유지: 외부 API 호출
        OAuth2UserInformationRequestDto oAuth2UserInformationRequestDto = new OAuth2UserInformationRequestDto(madpOAuth2UserInfo.providerId(), madpOAuth2UserInfo.mail(), madpOAuth2UserInfo.profile(), madpOAuth2UserInfo.name(), oAuth2Type);
        log.info("[OAuth2UserService] Calling userClient.getUserAuthStatus with: {}", oAuth2UserInformationRequestDto);
        
        UserAuthResponseDto userAuthResponseDto = userClient.getUserAuthStatus(oAuth2UserInformationRequestDto);
        log.info("[OAuth2UserService] UserAuthResponseDto received: {}", userAuthResponseDto);

        MadpOAuth2User result = MadpOAuth2User.builder()
                .userId(userAuthResponseDto.userId())
                .username(oAuth2UserInformationRequestDto.name())
                .role(userAuthResponseDto.requiresAdditionalAuth() ? Role.PARTIAL_AUTH : Role.USER)
                .attributes(oauth2User.getAttributes())
                .build();
        
        log.info("[OAuth2UserService] Final MadpOAuth2User: userId={}, username={}, role={}", 
                result.getUserId(), result.getUsername(), result.getRole());
        log.info("[OAuth2UserService] ===== loadUser END =====");
        
        return result;
    }

}
