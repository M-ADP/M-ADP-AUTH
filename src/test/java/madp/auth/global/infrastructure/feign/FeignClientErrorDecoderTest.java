package madp.auth.global.infrastructure.feign;

import feign.Request;
import feign.Response;
import madp.auth.global.infrastructure.feign.exception.FeignClientBadRequestException;
import madp.auth.global.infrastructure.feign.exception.FeignClientServiceUnavailableException;
import madp.auth.global.infrastructure.feign.exception.FeignClientTimeoutException;
import madp.auth.global.infrastructure.feign.exception.FeignClientUnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Feign Client 에러 디코더 테스트")
class FeignClientErrorDecoderTest {

    private FeignClientErrorDecoder feignClientErrorDecoder;
    private Request request;

    @BeforeEach
    void setUp() {
        feignClientErrorDecoder = new FeignClientErrorDecoder();
        request = Request.create(
                Request.HttpMethod.GET,
                "/test",
                Collections.emptyMap(),
                Request.Body.empty().asBytes(),
                null,
                null
        );
    }

    @Test
    @DisplayName("401 Unauthorized 응답 시 FeignClientUnauthorizedException이 발생한다")
    void shouldThrowFeignClientUnauthorizedExceptionWhen401() {
        try (Response response = Response.builder()
                .status(401)
                .reason("Unauthorized")
                .request(request)
                .headers(Collections.emptyMap())
                .build()) {

            assertThatThrownBy(() -> {
                throw feignClientErrorDecoder.decode("GET /test", response);
            }).isInstanceOf(FeignClientUnauthorizedException.class);
        }
    }

    @Test
    @DisplayName("408 Request Timeout 응답 시 FeignClientTimeoutException이 발생한다")
    void shouldThrowFeignClientTimeoutExceptionWhen408() {
        try (Response response = Response.builder()
                .status(408)
                .reason("Request Timeout")
                .request(request)
                .headers(Collections.emptyMap())
                .build()) {

            assertThatThrownBy(() -> {
                throw feignClientErrorDecoder.decode("GET /test", response);
            }).isInstanceOf(FeignClientTimeoutException.class);
        }
    }

    @Test
    @DisplayName("504 Gateway Timeout 응답 시 FeignClientTimeoutException이 발생한다")
    void shouldThrowFeignClientTimeoutExceptionWhen504() {
        try (Response response = Response.builder()
                .status(504)
                .reason("Gateway Timeout")
                .request(request)
                .headers(Collections.emptyMap())
                .build()) {

            assertThatThrownBy(() -> {
                throw feignClientErrorDecoder.decode("GET /test", response);
            }).isInstanceOf(FeignClientTimeoutException.class);
        }
    }

    @Test
    @DisplayName("400 Bad Request 응답 시 FeignClientBadRequestException이 발생한다")
    void shouldThrowFeignClientBadRequestExceptionWhen400() {
        try (Response response = Response.builder()
                .status(400)
                .reason("Bad Request")
                .request(request)
                .headers(Collections.emptyMap())
                .build()) {

            assertThatThrownBy(() -> {
                throw feignClientErrorDecoder.decode("GET /test", response);
            }).isInstanceOf(FeignClientBadRequestException.class);
        }
    }

    @Test
    @DisplayName("404 Not Found 응답 시 FeignClientBadRequestException이 발생한다")
    void shouldThrowFeignClientBadRequestExceptionWhen404() {
        try (Response response = Response.builder()
                .status(404)
                .reason("Not Found")
                .request(request)
                .headers(Collections.emptyMap())
                .build()) {

            assertThatThrownBy(() -> {
                throw feignClientErrorDecoder.decode("GET /test", response);
            }).isInstanceOf(FeignClientBadRequestException.class);
        }
    }

    @Test
    @DisplayName("499 클라이언트 에러 응답 시 FeignClientBadRequestException이 발생한다")
    void shouldThrowFeignClientBadRequestExceptionWhen499() {
        try (Response response = Response.builder()
                .status(499)
                .reason("Client Error")
                .request(request)
                .headers(Collections.emptyMap())
                .build()) {

            assertThatThrownBy(() -> {
                throw feignClientErrorDecoder.decode("GET /test", response);
            }).isInstanceOf(FeignClientBadRequestException.class);
        }
    }

    @Test
    @DisplayName("500 Internal Server Error 응답 시 FeignClientServiceUnavailableException이 발생한다")
    void shouldThrowFeignClientServiceUnavailableExceptionWhen500() {
        try (Response response = Response.builder()
                .status(500)
                .reason("Internal Server Error")
                .request(request)
                .headers(Collections.emptyMap())
                .build()) {

            assertThatThrownBy(() -> {
                throw feignClientErrorDecoder.decode("GET /test", response);
            }).isInstanceOf(FeignClientServiceUnavailableException.class);
        }
    }

    @Test
    @DisplayName("502 Bad Gateway 응답 시 FeignClientServiceUnavailableException이 발생한다")
    void shouldThrowFeignClientServiceUnavailableExceptionWhen502() {
        try (Response response = Response.builder()
                .status(502)
                .reason("Bad Gateway")
                .request(request)
                .headers(Collections.emptyMap())
                .build()) {

            assertThatThrownBy(() -> {
                throw feignClientErrorDecoder.decode("GET /test", response);
            }).isInstanceOf(FeignClientServiceUnavailableException.class);
        }
    }

    @Test
    @DisplayName("503 Service Unavailable 응답 시 FeignClientServiceUnavailableException이 발생한다")
    void shouldThrowFeignClientServiceUnavailableExceptionWhen503() {
        try (Response response = Response.builder()
                .status(503)
                .reason("Service Unavailable")
                .request(request)
                .headers(Collections.emptyMap())
                .build()) {

            assertThatThrownBy(() -> {
                throw feignClientErrorDecoder.decode("GET /test", response);
            }).isInstanceOf(FeignClientServiceUnavailableException.class);
        }
    }

    @Test
    @DisplayName("클라이언트 에러 범위 경계값 테스트 - 399는 클라이언트 에러가 아니다")
    void shouldNotBeClientErrorWhen399() {
        try (Response response = Response.builder()
                .status(399)
                .reason("Custom Status")
                .request(request)
                .headers(Collections.emptyMap())
                .build()) {

            assertThatThrownBy(() -> {
                throw feignClientErrorDecoder.decode("GET /test", response);
            }).isInstanceOf(FeignClientServiceUnavailableException.class);
        }
    }

    @Test
    @DisplayName("알려지지 않은 에러 코드 응답 시 FeignClientServiceUnavailableException이 발생한다")
    void shouldThrowFeignClientServiceUnavailableExceptionForUnknownError() {
        try (Response response = Response.builder()
                .status(999)
                .reason("Unknown Error")
                .request(request)
                .headers(Collections.emptyMap())
                .build()) {

            assertThatThrownBy(() -> {
                throw feignClientErrorDecoder.decode("GET /test", response);
            }).isInstanceOf(FeignClientServiceUnavailableException.class);
        }
    }
}
