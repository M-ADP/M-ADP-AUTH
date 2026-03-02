package madp.auth.global.configuration;

import lombok.RequiredArgsConstructor;
import madp.auth.global.enums.Role;
import madp.auth.domain.infrastructure.security.handler.MadpOAuth2FailureHandler;
import madp.auth.domain.infrastructure.security.handler.MadpOAuth2SuccessHandler;
import madp.auth.domain.infrastructure.security.service.MadpOAuth2UserService;
import madp.auth.global.properties.WebProperties;
import madp.auth.global.security.filter.MadpUserInfoExtractorFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final WebProperties webProperties;
    private final MadpOAuth2UserService madpOAuth2UserService;
    private final MadpOAuth2SuccessHandler madpOAuth2SuccessHandler;
    private final MadpOAuth2FailureHandler madpOAuth2FailureHandler;

    @Bean
    public PathMatcher pathMatcher() {return new AntPathMatcher();}

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .anonymous(anonymous -> anonymous
                        .principal(Role.GUEST.name())
                        .authorities(Role.GUEST.getValue())
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage(webProperties.getFrontEndUrl() + "/login")
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/auth/oauth2/authorization")
                        )
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/auth/oauth2/callback/*")
                        )
                        .successHandler(madpOAuth2SuccessHandler)
                        .failureHandler(madpOAuth2FailureHandler)
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                                .userService(madpOAuth2UserService)
                        )
                )
                .addFilterAfter(new MadpUserInfoExtractorFilter(), SecurityContextHolderFilter.class);

        return http.build();
    }
}
