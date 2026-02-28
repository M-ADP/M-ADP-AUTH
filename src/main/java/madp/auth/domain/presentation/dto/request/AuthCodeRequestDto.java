package madp.auth.domain.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AuthCodeRequestDto(
        @NotNull(message = "인증 요청에서 code(인증 코드)는 필수입니다.")
        @NotBlank(message = "인증 코드는 비어있을 수 없습니다")
        String code
) {}
