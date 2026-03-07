package madp.auth.domain.application.service;

import lombok.RequiredArgsConstructor;
import madp.auth.domain.domain.entity.AuthCodeEntity;
import madp.auth.domain.domain.entity.TokenEntity;
import madp.auth.domain.presentation.dto.response.AuthStatusResponse;
import madp.auth.global.enums.Role;
import madp.auth.domain.domain.repository.AuthCodeRepository;
import madp.auth.domain.domain.repository.TokenRepository;
import madp.auth.domain.exception.AuthCodeNotFoundException;
import madp.auth.domain.infrastructure.jwt.JwtManager;
import madp.auth.domain.presentation.dto.response.TokenResponseDto;
import madp.auth.global.properties.JwtProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtManager jwtManager;
    private final JwtProperties jwtProperties;
    private final TokenRepository tokenRepository;
    private final AuthCodeRepository authCodeRepository;

    public ResponseCookie deleteRefreshToken(String refreshToken) {
        Long userId = jwtManager.getUserId(refreshToken);

        tokenRepository.deleteById(userId);

        return jwtManager.createDeletedRefreshTokenCookie();
    }

    public TokenResponseDto reissueToken(String refreshToken) {
        Long userId = jwtManager.getUserId(refreshToken);

        tokenRepository.deleteById(userId);

        Role role = jwtManager.getRole(refreshToken);

        String newAccessToken = jwtManager.generateAccessToken(userId, role);
        String newRefreshToken = jwtManager.generateRefreshToken(userId, role);

        saveRefreshToken(newRefreshToken, userId);

        ResponseCookie newRefreshTokenCookie = jwtManager.createRefreshTokenCookie(newRefreshToken);

        return TokenResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshTokenCookie(newRefreshTokenCookie)
                .build();
    }

    public AuthStatusResponse getAuthStatusByAuthCode(String authCode) {
        AuthCodeEntity authCodeEntity = authCodeRepository.findByAuthCode(authCode).orElseThrow(AuthCodeNotFoundException::new);
        String accessToken = authCodeEntity.getAccessToken();
        Long userId = jwtManager.getUserId(accessToken);
        Role role = jwtManager.getRole(accessToken);
        Boolean isAuthenticated = Role.USER.equals(role);
        String refreshToken = getRefreshToken(userId, role);
        ResponseCookie refreshTokenCookie = jwtManager.createRefreshTokenCookie(refreshToken);

        authCodeRepository.delete(authCodeEntity);

        return AuthStatusResponse.builder()
                .isAuthenticated(isAuthenticated)
                .tokenResponseDto(
                        TokenResponseDto.builder()
                                .accessToken(accessToken)
                                .refreshTokenCookie(refreshTokenCookie)
                                .build()
                )
                .build();
    }

    private void saveRefreshToken(String refreshToken, Long userId) {
        TokenEntity tokenEntity = TokenEntity.builder()
                .token(refreshToken)
                .userId(userId)
                .expiration(jwtProperties.getRefreshExpiration())
                .build();
        tokenRepository.save(tokenEntity);
    }

    private String getRefreshToken(Long userId, Role role) {
        String refreshToken = jwtManager.generateRefreshToken(userId, role);

        Optional<TokenEntity> existingToken = tokenRepository.findById(userId);

        if (existingToken.isPresent()) {
            TokenEntity tokenEntity = existingToken.get();
            tokenEntity.updateToken(refreshToken);
            tokenRepository.save(tokenEntity);
        }
        else {
            saveRefreshToken(refreshToken, userId);
        }

        return refreshToken;
    }

}

