package madp.auth.global.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Web Properties 테스트")
class WebPropertiesTest {

    @Test
    @DisplayName("Web Properties를 생성하고 값들을 정상적으로 가져올 수 있다")
    void shouldCreateWebPropertiesAndGetValues() {
        // Given: 웹 설정 정보가 준비되어 있다
        String frontEndUrl = "https://frontend.example.com";
        String backEndUrl = "https://backend.example.com";

        // When: WebProperties를 생성한다
        WebProperties webProperties = new WebProperties(frontEndUrl, backEndUrl);

        // Then: 모든 값들이 정상적으로 설정된다
        assertThat(webProperties.getFrontEndUrl()).isEqualTo(frontEndUrl);
        assertThat(webProperties.getBackEndUrl()).isEqualTo(backEndUrl);
    }

    @Test
    @DisplayName("localhost URL로 Web Properties를 생성할 수 있다")
    void shouldCreateWebPropertiesWithLocalhostUrls() {
        // Given: localhost URL이 준비되어 있다
        String frontEndUrl = "http://localhost:3000";
        String backEndUrl = "http://localhost:8080";

        // When: WebProperties를 생성한다
        WebProperties webProperties = new WebProperties(frontEndUrl, backEndUrl);

        // Then: localhost URL이 정상적으로 설정된다
        assertThat(webProperties.getFrontEndUrl()).isEqualTo(frontEndUrl);
        assertThat(webProperties.getBackEndUrl()).isEqualTo(backEndUrl);
    }

    @Test
    @DisplayName("포트가 포함된 URL로 Web Properties를 생성할 수 있다")
    void shouldCreateWebPropertiesWithPortUrls() {
        // Given: 포트가 포함된 URL이 준비되어 있다
        String frontEndUrl = "https://frontend.example.com:443";
        String backEndUrl = "https://backend.example.com:8443";

        // When: WebProperties를 생성한다
        WebProperties webProperties = new WebProperties(frontEndUrl, backEndUrl);

        // Then: 포트가 포함된 URL이 정상적으로 설정된다
        assertThat(webProperties.getFrontEndUrl()).isEqualTo(frontEndUrl);
        assertThat(webProperties.getBackEndUrl()).isEqualTo(backEndUrl);
    }

    @Test
    @DisplayName("경로가 포함된 URL로 Web Properties를 생성할 수 있다")
    void shouldCreateWebPropertiesWithPathUrls() {
        // Given: 경로가 포함된 URL이 준비되어 있다
        String frontEndUrl = "https://example.com/frontend/app";
        String backEndUrl = "https://example.com/backend/api";

        // When: WebProperties를 생성한다
        WebProperties webProperties = new WebProperties(frontEndUrl, backEndUrl);

        // Then: 경로가 포함된 URL이 정상적으로 설정된다
        assertThat(webProperties.getFrontEndUrl()).isEqualTo(frontEndUrl);
        assertThat(webProperties.getBackEndUrl()).isEqualTo(backEndUrl);
    }

    @Test
    @DisplayName("빈 문자열 URL로 Web Properties를 생성할 수 있다")
    void shouldCreateWebPropertiesWithEmptyUrls() {
        // Given: 빈 문자열 URL이 주어진다
        String emptyUrl = "";

        // When: WebProperties를 생성한다
        WebProperties webProperties = new WebProperties(emptyUrl, emptyUrl);

        // Then: 빈 문자열도 정상적으로 설정된다
        assertThat(webProperties.getFrontEndUrl()).isEqualTo(emptyUrl);
        assertThat(webProperties.getBackEndUrl()).isEqualTo(emptyUrl);
    }

    @Test
    @DisplayName("null 값으로 Web Properties를 생성할 수 있다")
    void shouldCreateWebPropertiesWithNullUrls() {
        // Given & When: null 값들이 주어지면서, WebProperties를 생성한다
        WebProperties webProperties = new WebProperties(null, null);

        // Then: null 값들도 정상적으로 설정된다
        assertThat(webProperties.getFrontEndUrl()).isNull();
        assertThat(webProperties.getBackEndUrl()).isNull();
    }

    @Test
    @DisplayName("서로 다른 프로토콜의 URL로 Web Properties를 생성할 수 있다")
    void shouldCreateWebPropertiesWithDifferentProtocols() {
        // Given: 서로 다른 프로토콜의 URL이 준비되어 있다
        String frontEndUrl = "https://secure.example.com";
        String backEndUrl = "http://api.example.com";

        // When: WebProperties를 생성한다
        WebProperties webProperties = new WebProperties(frontEndUrl, backEndUrl);

        // Then: 서로 다른 프로토콜의 URL이 정상적으로 설정된다
        assertThat(webProperties.getFrontEndUrl()).isEqualTo(frontEndUrl);
        assertThat(webProperties.getBackEndUrl()).isEqualTo(backEndUrl);
        assertThat(webProperties.getFrontEndUrl()).startsWith("https://");
        assertThat(webProperties.getBackEndUrl()).startsWith("http://");
    }

    @Test
    @DisplayName("동일한 URL로 Web Properties를 생성할 수 있다")
    void shouldCreateWebPropertiesWithSameUrls() {
        // Given: 동일한 URL이 준비되어 있다
        String sameUrl = "https://example.com";

        // When: WebProperties를 생성한다
        WebProperties webProperties = new WebProperties(sameUrl, sameUrl);

        // Then: 동일한 URL이 정상적으로 설정된다
        assertThat(webProperties.getFrontEndUrl()).isEqualTo(sameUrl);
        assertThat(webProperties.getBackEndUrl()).isEqualTo(sameUrl);
        assertThat(webProperties.getFrontEndUrl()).isEqualTo(webProperties.getBackEndUrl());
    }

    @Test
    @DisplayName("IP 주소 URL로 Web Properties를 생성할 수 있다")
    void shouldCreateWebPropertiesWithIpUrls() {
        // Given: IP 주소 URL이 준비되어 있다
        String frontEndUrl = "http://192.168.1.100:3000";
        String backEndUrl = "http://192.168.1.100:8080";

        // When: WebProperties를 생성한다
        WebProperties webProperties = new WebProperties(frontEndUrl, backEndUrl);

        // Then: IP 주소 URL이 정상적으로 설정된다
        assertThat(webProperties.getFrontEndUrl()).isEqualTo(frontEndUrl);
        assertThat(webProperties.getBackEndUrl()).isEqualTo(backEndUrl);
    }

    @Test
    @DisplayName("서브도메인이 포함된 URL로 Web Properties를 생성할 수 있다")
    void shouldCreateWebPropertiesWithSubdomainUrls() {
        // Given: 서브도메인이 포함된 URL이 준비되어 있다
        String frontEndUrl = "https://app.frontend.example.com";
        String backEndUrl = "https://api.backend.example.com";

        // When: WebProperties를 생성한다
        WebProperties webProperties = new WebProperties(frontEndUrl, backEndUrl);

        // Then: 서브도메인이 포함된 URL이 정상적으로 설정된다
        assertThat(webProperties.getFrontEndUrl()).isEqualTo(frontEndUrl);
        assertThat(webProperties.getBackEndUrl()).isEqualTo(backEndUrl);
        assertThat(webProperties.getFrontEndUrl()).contains("app.frontend");
        assertThat(webProperties.getBackEndUrl()).contains("api.backend");
    }

    @Test
    @DisplayName("쿼리 매개변수가 포함된 URL로 Web Properties를 생성할 수 있다")
    void shouldCreateWebPropertiesWithQueryParameters() {
        // Given: 쿼리 매개변수가 포함된 URL이 준비되어 있다
        String frontEndUrl = "https://example.com?env=prod&version=1.0";
        String backEndUrl = "https://api.example.com?service=auth&region=us-east";

        // When: WebProperties를 생성한다
        WebProperties webProperties = new WebProperties(frontEndUrl, backEndUrl);

        // Then: 쿼리 매개변수가 포함된 URL이 정상적으로 설정된다
        assertThat(webProperties.getFrontEndUrl()).isEqualTo(frontEndUrl);
        assertThat(webProperties.getBackEndUrl()).isEqualTo(backEndUrl);
        assertThat(webProperties.getFrontEndUrl()).contains("?env=prod&version=1.0");
        assertThat(webProperties.getBackEndUrl()).contains("?service=auth&region=us-east");
    }
}