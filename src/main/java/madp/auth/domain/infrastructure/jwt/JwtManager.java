package madp.auth.domain.infrastructure.jwt;

import io.jsonwebtoken.Jwts;
import madp.auth.global.enums.Role;
import madp.auth.domain.infrastructure.jwt.constants.JwtConstants;
import madp.auth.global.properties.JwtProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.Cookie;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

@Component
public class JwtManager {
    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    @Autowired
    public JwtManager(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = new SecretKeySpec(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm()
        );
    }

    public Long getUserId(String token) {
        String userId = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        return Long.parseLong(userId);
    }

    public Role getRole(String token) {
        String role = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role").toString();
        return Role.valueOf(role);
    }

    public String generateAccessToken(Long userId, Role role) {
        return generateToken(userId, role, jwtProperties.getAccessExpiration());
    }

    public String generateRefreshToken(Long userId, Role role) {
        return generateToken(userId, role, jwtProperties.getRefreshExpiration());
    }

    private String generateToken(Long userId, Role role, Long expiration) {
        long now = System.currentTimeMillis();
        Date expirationTime = new Date(now + expiration);

        return Jwts.builder()
                .subject(userId.toString())
                .claim(JwtConstants.CLAIM_MADE_BY_KEY, JwtConstants.CLAIM_MADE_BY_VALUE)
                .claim(JwtConstants.CLAIM_ROLE_KEY, role)
                .issuedAt(new Date(now))
                .issuer(jwtProperties.getIssuer())
                .expiration(expirationTime)
                .signWith(secretKey)
                .compact();
    }

    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from(JwtConstants.REFRESH_TOKEN_COOKIE_HEADER, refreshToken)
                .maxAge(Duration.ofMillis(jwtProperties.getRefreshExpiration()))
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite(Cookie.SameSite.STRICT.attributeValue())
                .build();
    }

    public ResponseCookie createDeletedRefreshTokenCookie() {
        return ResponseCookie.from(JwtConstants.REFRESH_TOKEN_COOKIE_HEADER, "")
                .maxAge(0)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite(Cookie.SameSite.STRICT.attributeValue())
                .build();
    }

}
