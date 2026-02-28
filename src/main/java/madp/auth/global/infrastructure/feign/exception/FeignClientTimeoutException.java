package madp.auth.global.infrastructure.feign.exception;

import madp.auth.global.exception.MadpSystemError;
import org.springframework.http.HttpStatus;

public class FeignClientTimeoutException extends MadpSystemError {
    public FeignClientTimeoutException() {
        super("외부 서비스 응답 시간이 초과되었습니다.", HttpStatus.GATEWAY_TIMEOUT);
    }
}
