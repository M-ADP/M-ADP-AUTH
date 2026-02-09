package madp.auth.global.exception;

import madp.auth.domain.exception.AuthCodeNotFoundException;
import madp.auth.domain.exception.RefreshTokenNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Global 예외 핸들러 테스트")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("MissingRequestCookieException 처리 시 400 Bad Request 응답을 반환한다")
    void shouldHandleMissingRequestCookieExceptionWith400() {
        // When: 예외를 처리한다
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMissingRequestCookieException();

        // Then: 400 Bad Request 응답이 반환된다
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("필수 쿠키가 누락되었습니다.");
    }

    @Test
    @DisplayName("MadpException 처리 시 해당 예외의 상태 코드로 응답을 반환한다")
    void shouldHandleMadpExceptionWithExceptionStatus() {
        // Given: MadpException이 발생한다
        MadpException exception = new AuthCodeNotFoundException();

        // When: 예외를 처리한다
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMadpException(exception);

        // Then: 해당 예외의 상태 코드로 응답이 반환된다
        assertThat(response.getStatusCode()).isEqualTo(exception.getStatus());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(exception.getStatus().value());
        assertThat(response.getBody().getMessage()).isEqualTo(exception.getMessage());
    }

    @Test
    @DisplayName("다른 MadpException도 올바르게 처리된다")
    void shouldHandleAnotherMadpExceptionCorrectly() {
        // Given: 다른 MadpException이 발생한다
        MadpException exception = new RefreshTokenNotFoundException();

        // When: 예외를 처리한다
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMadpException(exception);

        // Then: 해당 예외의 상태 코드로 응답이 반환된다
        assertThat(response.getStatusCode()).isEqualTo(exception.getStatus());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(exception.getStatus().value());
        assertThat(response.getBody().getMessage()).isEqualTo(exception.getMessage());
    }

    @Test
    @DisplayName("RuntimeException 처리 시 500 Internal Server Error 응답을 반환한다")
    void shouldHandleRuntimeExceptionWith500() {
        // Given: RuntimeException이 발생한다
        RuntimeException exception = new RuntimeException("Runtime error occurred");

        // When: 예외를 처리한다
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleRuntimeException(exception);

        // Then: 500 Internal Server Error 응답이 반환된다
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo("Runtime error occurred");
    }

    @Test
    @DisplayName("일반 Exception 처리 시 500 Internal Server Error 응답을 반환한다")
    void shouldHandleGeneralExceptionWith500() {
        // Given: 일반 Exception이 발생한다
        Exception exception = new Exception("General error occurred");

        // When: 예외를 처리한다
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleException(exception);

        // Then: 500 Internal Server Error 응답이 반환된다
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo("General error occurred");
    }

    @Test
    @DisplayName("null 메시지를 가진 RuntimeException 올바르게 처리된다")
    void shouldHandleRuntimeExceptionWithNullMessage() {
        // Given: null 메시지를 가진 RuntimeException이 발생한다
        RuntimeException exception = new RuntimeException((String) null);

        // When: 예외를 처리한다
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleRuntimeException(exception);

        // Then: 500 Internal Server Error 응답이 반환된다
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isNull();
    }

    @Test
    @DisplayName("null 메세지를 가진 Exception도 올바르게 처리된다")
    void shouldHandleGeneralException() {
        // Given: null 메시지를 가진 일반 Exception이 발생한다
        Exception exception = new Exception((String) null);

        // When: 예외를 처리한다
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleException(exception);

        // Then: 500 Internal Server Error 응답이 반환된다
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isNull();
    }

    @Test
    @DisplayName("NullPointerException 예외가 처리된다")
    void shouldHandleNullPointerExceptionAsRuntimeException() {
        // Given: NullPointerException이 발생한다
        NullPointerException exception = new NullPointerException("Null pointer access");

        // When: RuntimeException 핸들러로 처리한다
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleNullPointerException(exception);

        // Then: 500 Internal Server Error 응답이 반환된다
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo("Null pointer access");
    }

    @Test
    @DisplayName("IllegalArgumentException 예외가 처리된다")
    void shouldHandleIllegalArgumentException() {
        // Given & When: IllegalArgumentException이 발생하면, handleIllegalArgumentException 핸들러로 처리한다
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException();

        // Then: 500 Internal Server Error 응답이 반환된다
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("유효하지 않은 요청 파라미터입니다.");
    }
}