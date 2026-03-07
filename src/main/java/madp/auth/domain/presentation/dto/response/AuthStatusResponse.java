package madp.auth.domain.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record AuthStatusResponse(
        @JsonProperty("is_authenticated")
        Boolean isAuthenticated,

        @JsonProperty("token_response")
        TokenResponseDto tokenResponseDto
) {}
