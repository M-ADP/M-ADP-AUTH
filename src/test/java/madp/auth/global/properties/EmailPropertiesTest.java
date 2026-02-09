package madp.auth.global.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Email Properties 테스트")
class EmailPropertiesTest {

    @Test
    @DisplayName("Email Properties를 생성하고 허용 도메인을 정상적으로 가져올 수 있다")
    void shouldCreateEmailPropertiesAndGetAllowedDomain() {
        // Given: 허용된 이메일 도메인이 준비되어 있다
        String allowedDomain = "@company.com";

        // When: EmailProperties를 생성한다
        EmailProperties emailProperties = new EmailProperties(allowedDomain);

        // Then: 허용된 도메인이 정상적으로 설정된다
        assertThat(emailProperties.getAllowedDomain()).isEqualTo(allowedDomain);
    }

    @Test
    @DisplayName("@ 기호가 포함된 도메인으로 Email Properties를 생성할 수 있다")
    void shouldCreateEmailPropertiesWithAtSymbolDomain() {
        // Given: @ 기호가 포함된 도메인이 준비되어 있다
        String domainWithAt = "@example.org";

        // When: EmailProperties를 생성한다
        EmailProperties emailProperties = new EmailProperties(domainWithAt);

        // Then: @ 기호가 포함된 도메인이 정상적으로 설정된다
        assertThat(emailProperties.getAllowedDomain()).isEqualTo(domainWithAt);
        assertThat(emailProperties.getAllowedDomain()).startsWith("@");
    }

    @Test
    @DisplayName("@ 기호가 없는 도메인으로 Email Properties를 생성할 수 있다")
    void shouldCreateEmailPropertiesWithoutAtSymbolDomain() {
        // Given: @ 기호가 없는 도메인이 준비되어 있다
        String domainWithoutAt = "example.net";

        // When: EmailProperties를 생성한다
        EmailProperties emailProperties = new EmailProperties(domainWithoutAt);

        // Then: @ 기호가 없는 도메인도 정상적으로 설정된다
        assertThat(emailProperties.getAllowedDomain()).isEqualTo(domainWithoutAt);
        assertThat(emailProperties.getAllowedDomain()).doesNotStartWith("@");
    }

    @Test
    @DisplayName("서브도메인이 포함된 도메인으로 Email Properties를 생성할 수 있다")
    void shouldCreateEmailPropertiesWithSubdomain() {
        // Given: 서브도메인이 포함된 도메인이 준비되어 있다
        String subdomainDomain = "@mail.company.example.com";

        // When: EmailProperties를 생성한다
        EmailProperties emailProperties = new EmailProperties(subdomainDomain);

        // Then: 서브도메인이 포함된 도메인이 정상적으로 설정된다
        assertThat(emailProperties.getAllowedDomain()).isEqualTo(subdomainDomain);
        assertThat(emailProperties.getAllowedDomain()).contains("mail.company");
    }

    @Test
    @DisplayName("짧은 도메인으로 Email Properties를 생성할 수 있다")
    void shouldCreateEmailPropertiesWithShortDomain() {
        // Given: 짧은 도메인이 준비되어 있다
        String shortDomain = "@io";

        // When: EmailProperties를 생성한다
        EmailProperties emailProperties = new EmailProperties(shortDomain);

        // Then: 짧은 도메인이 정상적으로 설정된다
        assertThat(emailProperties.getAllowedDomain()).isEqualTo(shortDomain);
        assertThat(emailProperties.getAllowedDomain()).hasSize(3);
    }

    @Test
    @DisplayName("긴 도메인으로 Email Properties를 생성할 수 있다")
    void shouldCreateEmailPropertiesWithLongDomain() {
        // Given: 긴 도메인이 준비되어 있다
        String longDomain = "@very.long.subdomain.example.corporation.international.com";

        // When: EmailProperties를 생성한다
        EmailProperties emailProperties = new EmailProperties(longDomain);

        // Then: 긴 도메인이 정상적으로 설정된다
        assertThat(emailProperties.getAllowedDomain()).isEqualTo(longDomain);
        assertThat(emailProperties.getAllowedDomain()).contains("very.long.subdomain");
    }

    @Test
    @DisplayName("빈 문자열 도메인으로 Email Properties를 생성할 수 있다")
    void shouldCreateEmailPropertiesWithEmptyDomain() {
        // Given: 빈 문자열 도메인이 주어진다
        String emptyDomain = "";

        // When: EmailProperties를 생성한다
        EmailProperties emailProperties = new EmailProperties(emptyDomain);

        // Then: 빈 문자열도 정상적으로 설정된다
        assertThat(emailProperties.getAllowedDomain()).isEqualTo(emptyDomain);
        assertThat(emailProperties.getAllowedDomain()).isEmpty();
    }

    @Test
    @DisplayName("null 도메인으로 Email Properties를 생성할 수 있다")
    void shouldCreateEmailPropertiesWithNullDomain() {
        // Given & When: null 도메인이 주어지고, EmailProperties를 생성한다
        EmailProperties emailProperties = new EmailProperties(null);

        // Then: null 값도 정상적으로 설정된다
        assertThat(emailProperties.getAllowedDomain()).isNull();
    }

    @Test
    @DisplayName("숫자가 포함된 도메인으로 Email Properties를 생성할 수 있다")
    void shouldCreateEmailPropertiesWithNumericDomain() {
        // Given: 숫자가 포함된 도메인이 준비되어 있다
        String numericDomain = "@company123.example456.com";

        // When: EmailProperties를 생성한다
        EmailProperties emailProperties = new EmailProperties(numericDomain);

        // Then: 숫자가 포함된 도메인이 정상적으로 설정된다
        assertThat(emailProperties.getAllowedDomain()).isEqualTo(numericDomain);
        assertThat(emailProperties.getAllowedDomain()).contains("123");
        assertThat(emailProperties.getAllowedDomain()).contains("456");
    }

    @Test
    @DisplayName("하이픈이 포함된 도메인으로 Email Properties를 생성할 수 있다")
    void shouldCreateEmailPropertiesWithHyphenDomain() {
        // Given: 하이픈이 포함된 도메인이 준비되어 있다
        String hyphenDomain = "@my-company.co.kr";

        // When: EmailProperties를 생성한다
        EmailProperties emailProperties = new EmailProperties(hyphenDomain);

        // Then: 하이픈이 포함된 도메인이 정상적으로 설정된다
        assertThat(emailProperties.getAllowedDomain()).isEqualTo(hyphenDomain);
        assertThat(emailProperties.getAllowedDomain()).contains("-");
    }

    @Test
    @DisplayName("국제 도메인으로 Email Properties를 생성할 수 있다")
    void shouldCreateEmailPropertiesWithInternationalDomain() {
        // Given: 다양한 국가 코드 도메인이 준비되어 있다
        String[] internationalDomains = {
                "@company.co.uk",
                "@hi.com.mx",
                "@company.com.au",
                "@société.fr",
                "@heo.de"
        };

        for (String domain : internationalDomains) {
            // When: EmailProperties를 생성한다
            EmailProperties emailProperties = new EmailProperties(domain);

            // Then: 국제 도메인이 정상적으로 설정된다
            assertThat(emailProperties.getAllowedDomain()).isEqualTo(domain);
        }
    }

    @Test
    @DisplayName("점으로만 구성된 도메인으로 Email Properties를 생성할 수 있다")
    void shouldCreateEmailPropertiesWithDotsOnlyDomain() {
        // Given: 점으로만 구성된 도메인이 준비되어 있다
        String dotsOnlyDomain = "...";

        // When: EmailProperties를 생성한다
        EmailProperties emailProperties = new EmailProperties(dotsOnlyDomain);

        // Then: 점으로만 구성된 도메인도 정상적으로 설정된다
        assertThat(emailProperties.getAllowedDomain()).isEqualTo(dotsOnlyDomain);
        assertThat(emailProperties.getAllowedDomain()).isEqualTo(dotsOnlyDomain);
        assertThat(emailProperties.getAllowedDomain().chars().allMatch(c -> c == '.')).isTrue();
    }
}