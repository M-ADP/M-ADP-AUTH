package madp.auth.domain.infrastructure.client.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record  UserAuthResponseDto(
        @JsonProperty("user_id")
        Long userId,
        @JsonProperty("requires_additional_auth")
        Boolean requiresAdditionalAuth
) {}