package madp.auth.domain.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import madp.auth.domain.application.service.AuthService;
import madp.auth.domain.exception.RefreshTokenNotFoundException;
import madp.auth.domain.infrastructure.jwt.constants.JwtConstants;
import madp.auth.domain.presentation.dto.request.AuthCodeRequestDto;
import madp.auth.domain.presentation.dto.response.TokenResponseDto;
import madp.auth.global.properties.JwtProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtProperties jwtProperties;
    private final AuthService authService;

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> getJwks() {
        Map<String, Object> jwks = new HashMap<>();
        jwks.put("kty", "oct");
        jwks.put("alg", "HS256");
        jwks.put("use", "sig");
        jwks.put("kid", jwtProperties.getKeyId());

        return Map.of("keys", List.of(jwks));
    }

    @PostMapping("/reissue")
    public ResponseEntity<Map<String, String>> reissue(@CookieValue(value = JwtConstants.REFRESH_TOKEN_COOKIE_HEADER, required = false) String refreshToken) {
        if(refreshToken == null) throw new RefreshTokenNotFoundException();
        TokenResponseDto tokenResponseDto = authService.reissueToken(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, tokenResponseDto.refreshTokenCookie().toString())
                .body(Map.of("access_token", tokenResponseDto.accessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(value = JwtConstants.REFRESH_TOKEN_COOKIE_HEADER, required = false) String refreshToken) {
        if(refreshToken == null) throw new RefreshTokenNotFoundException();
        ResponseCookie deletedRefreshTokenCookie = authService.deleteRefreshToken(refreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deletedRefreshTokenCookie.toString())
                .build();
    }

    @PostMapping("/code")
    public ResponseEntity<Map<String, String>> authCode(@RequestBody @Valid AuthCodeRequestDto authCodeRequestDto) {
        String accessToken = authService.getAccessTokenByAuthCode(authCodeRequestDto.code());
        return ResponseEntity.ok().body(Map.of("access_token", accessToken));
    }
}
