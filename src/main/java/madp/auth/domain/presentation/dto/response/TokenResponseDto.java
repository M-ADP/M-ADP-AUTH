package madp.auth.domain.presentation.dto.response;

import lombok.Builder;
import org.springframework.http.ResponseCookie;

@Builder
public record TokenResponseDto(
        String accessToken,
        ResponseCookie refreshTokenCookie
) {}
