package madp.auth.domain.domain.repository;

import madp.auth.domain.domain.entity.AuthCodeEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthCode 저장소 테스트")
class AuthCodeRepositoryTest {

    @Autowired
    private AuthCodeRepository authCodeRepository;

    @Test
    @DisplayName("인증 코드로 AuthCodeEntity를 찾을 수 있다")
    void shouldFindAuthCodeEntityByAuthCode() {
        // Given: 인증 코드 엔티티가 저장되어 있다
        AuthCodeEntity authCodeEntity = AuthCodeEntity.builder()
                .authCode("test-auth-code")
                .accessToken("test-access-token")
                .timeToLive(300L)
                .build();
        authCodeRepository.save(authCodeEntity);

        // When: 인증 코드로 엔티티를 조회한다
        Optional<AuthCodeEntity> result = authCodeRepository.findByAuthCode("test-auth-code");

        // Then: 엔티티가 정상적으로 조회된다
        assertThat(result).isPresent();
        assertThat(result.get().getAuthCode()).isEqualTo("test-auth-code");
        assertThat(result.get().getAccessToken()).isEqualTo("test-access-token");
        assertThat(result.get().getTimeToLive()).isEqualTo(300L);
    }

    @Test
    @DisplayName("존재하지 않는 인증 코드로 찾으면 빈 결과가 반환된다")
    void shouldReturnEmptyWhenAuthCodeNotExists() {
        // Given: 존재하지 않는 인증 코드가 주어진다
        
        // When: 존재하지 않는 인증 코드로 조회한다
        Optional<AuthCodeEntity> result = authCodeRepository.findByAuthCode("non-existent-code");

        // Then: 빈 결과가 반환된다
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("AuthCodeEntity를 성공적으로 저장할 수 있다")
    void shouldSaveAuthCodeEntitySuccessfully() {
        // Given: 새로운 인증 코드 엔티티가 준비되어 있다
        AuthCodeEntity authCodeEntity = AuthCodeEntity.builder()
                .authCode("save-test-code")
                .accessToken("save-test-token")
                .timeToLive(600L)
                .build();

        // When: 엔티티를 저장한다
        AuthCodeEntity savedEntity = authCodeRepository.save(authCodeEntity);

        // Then: 엔티티가 성공적으로 저장된다
        assertThat(savedEntity).isNotNull();
        assertThat(savedEntity.getAuthCode()).isEqualTo("save-test-code");
        assertThat(savedEntity.getAccessToken()).isEqualTo("save-test-token");
        
        // 저장된 엔티티를 다시 조회할 수 있다
        Optional<AuthCodeEntity> foundEntity = authCodeRepository.findByAuthCode("save-test-code");
        assertThat(foundEntity).isPresent();
        assertThat(foundEntity.get().getAuthCode()).isEqualTo("save-test-code");
    }

    @Test
    @DisplayName("AuthCodeEntity를 성공적으로 삭제할 수 있다")
    void shouldDeleteAuthCodeEntitySuccessfully() {
        // Given: 인증 코드 엔티티가 저장되어 있다
        AuthCodeEntity authCodeEntity = AuthCodeEntity.builder()
                .authCode("delete-test-code")
                .accessToken("delete-test-token")
                .timeToLive(300L)
                .build();
        authCodeRepository.save(authCodeEntity);

        // When: 엔티티를 삭제한다
        authCodeRepository.delete(authCodeEntity);

        // Then: 엔티티가 성공적으로 삭제된다
        Optional<AuthCodeEntity> result = authCodeRepository.findByAuthCode("delete-test-code");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("동일한 인증 코드로 기존 엔티티를 업데이트할 수 있다")
    void shouldUpdateExistingAuthCodeEntity() {
        // Given: 기존 인증 코드 엔티티가 저장되어 있다
        AuthCodeEntity originalEntity = AuthCodeEntity.builder()
                .authCode("update-test-code")
                .accessToken("original-token")
                .timeToLive(300L)
                .build();
        authCodeRepository.save(originalEntity);

        // When: 같은 인증 코드로 새로운 액세스 토큰을 가진 엔티티를 저장한다
        AuthCodeEntity updatedEntity = AuthCodeEntity.builder()
                .authCode("update-test-code")
                .accessToken("updated-token")
                .timeToLive(600L)
                .build();
        authCodeRepository.save(updatedEntity);

        // Then: 엔티티가 업데이트된다
        Optional<AuthCodeEntity> result = authCodeRepository.findByAuthCode("update-test-code");
        assertThat(result).isPresent();
        assertThat(result.get().getAccessToken()).isEqualTo("updated-token");
        assertThat(result.get().getTimeToLive()).isEqualTo(600L);
    }

    @Test
    @DisplayName("여러 AuthCodeEntity를 저장하고 각각 개별적으로 조회할 수 있다")
    void shouldSaveAndRetrieveMultipleAuthCodeEntities() {
        // Given: 여러 인증 코드 엔티티가 준비되어 있다
        AuthCodeEntity entity1 = AuthCodeEntity.builder()
                .authCode("code-1")
                .accessToken("token-1")
                .timeToLive(300L)
                .build();
        
        AuthCodeEntity entity2 = AuthCodeEntity.builder()
                .authCode("code-2")
                .accessToken("token-2")
                .timeToLive(400L)
                .build();

        // When: 여러 엔티티를 저장한다
        authCodeRepository.save(entity1);
        authCodeRepository.save(entity2);

        // Then: 각 엔티티를 개별적으로 조회할 수 있다
        Optional<AuthCodeEntity> result1 = authCodeRepository.findByAuthCode("code-1");
        Optional<AuthCodeEntity> result2 = authCodeRepository.findByAuthCode("code-2");
        
        assertThat(result1).isPresent();
        assertThat(result1.get().getAccessToken()).isEqualTo("token-1");
        
        assertThat(result2).isPresent();
        assertThat(result2.get().getAccessToken()).isEqualTo("token-2");
    }
}