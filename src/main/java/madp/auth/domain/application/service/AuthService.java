package madp.auth.domain.application.service;

import lombok.RequiredArgsConstructor;
import madp.auth.domain.domain.entity.AuthCodeEntity;
import madp.auth.domain.domain.entity.TokenEntity;
import madp.auth.global.enums.Role;
import madp.auth.domain.domain.repository.AuthCodeRepository;
import madp.auth.domain.domain.repository.TokenRepository;
import madp.auth.domain.exception.AuthCodeNotFoundException;
import madp.auth.domain.infrastructure.jwt.JwtManager;
import madp.auth.domain.presentation.dto.response.TokenResponseDto;
import madp.auth.global.properties.JwtProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtManager jwtManager;
    private final JwtProperties jwtProperties;
    private final TokenRepository tokenRepository;
    private final AuthCodeRepository authCodeRepository;

    public ResponseCookie deleteRefreshToken(String refreshToken) {
        tokenRepository.deleteById(refreshToken);

        return jwtManager.createDeletedRefreshTokenCookie();
    }

    public TokenResponseDto reissueToken(String refreshToken) {
        tokenRepository.deleteById(refreshToken);

        Long userId = jwtManager.getUserId(refreshToken);
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

    public String getAccessTokenByAuthCode(String authCode) {
        AuthCodeEntity authCodeEntity = authCodeRepository.findByAuthCode(authCode).orElseThrow(AuthCodeNotFoundException::new);
        String accessToken = authCodeEntity.getAccessToken();
        authCodeRepository.delete(authCodeEntity);
        return accessToken;
    }

    private void saveRefreshToken(String refreshToken, Long userId) {
        TokenEntity tokenEntity = TokenEntity.builder()
                .token(refreshToken)
                .userId(userId)
                .expiration(jwtProperties.getRefreshExpiration())
                .build();
        tokenRepository.save(tokenEntity);
    }
}

