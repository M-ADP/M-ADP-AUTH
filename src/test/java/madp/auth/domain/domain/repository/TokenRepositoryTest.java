package madp.auth.domain.domain.repository;

import madp.auth.domain.domain.entity.TokenEntity;
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
@DisplayName("Token 저장소 테스트")
class TokenRepositoryTest {

    @Autowired
    private TokenRepository tokenRepository;

    @Test
    @DisplayName("TokenEntity를 성공적으로 저장할 수 있다")
    void shouldSaveTokenEntitySuccessfully() {
        // Given: 새로운 토큰 엔티티가 준비되어 있다
        TokenEntity tokenEntity = TokenEntity.builder()
                .token("test-refresh-token")
                .userId(1L)
                .expiration(86400L)
                .build();

        // When: 엔티티를 저장한다
        TokenEntity savedEntity = tokenRepository.save(tokenEntity);

        // Then: 엔티티가 성공적으로 저장된다
        assertThat(savedEntity).isNotNull();
        assertThat(savedEntity.getToken()).isEqualTo("test-refresh-token");
        assertThat(savedEntity.getUserId()).isEqualTo(1L);
        assertThat(savedEntity.getExpiration()).isEqualTo(86400L);
    }

    @Test
    @DisplayName("토큰 ID로 TokenEntity를 찾을 수 있다")
    void shouldFindTokenEntityById() {
        // Given: 토큰 엔티티가 저장되어 있다
        TokenEntity tokenEntity = TokenEntity.builder()
                .token("find-test-token")
                .userId(2L)
                .expiration(7200L)
                .build();
        tokenRepository.save(tokenEntity);

        // When: 토큰 ID로 엔티티를 조회한다
        Optional<TokenEntity> result = tokenRepository.findById("find-test-token");

        // Then: 엔티티가 정상적으로 조회된다
        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo("find-test-token");
        assertThat(result.get().getUserId()).isEqualTo(2L);
        assertThat(result.get().getExpiration()).isEqualTo(7200L);
    }

    @Test
    @DisplayName("존재하지 않는 토큰 ID로 찾으면 빈 결과가 반환된다")
    void shouldReturnEmptyWhenTokenNotExists() {
        // Given: 존재하지 않는 토큰 ID가 주어진다
        
        // When: 존재하지 않는 토큰 ID로 조회한다
        Optional<TokenEntity> result = tokenRepository.findById("non-existent-token");

        // Then: 빈 결과가 반환된다
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("TokenEntity를 성공적으로 삭제할 수 있다")
    void shouldDeleteTokenEntitySuccessfully() {
        // Given: 토큰 엔티티가 저장되어 있다
        TokenEntity tokenEntity = TokenEntity.builder()
                .token("delete-test-token")
                .userId(3L)
                .expiration(3600L)
                .build();
        tokenRepository.save(tokenEntity);

        // When: 엔티티를 삭제한다
        tokenRepository.delete(tokenEntity);

        // Then: 엔티티가 성공적으로 삭제된다
        Optional<TokenEntity> result = tokenRepository.findById("delete-test-token");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("토큰 ID로 TokenEntity를 삭제할 수 있다")
    void shouldDeleteTokenEntityById() {
        // Given: 토큰 엔티티가 저장되어 있다
        TokenEntity tokenEntity = TokenEntity.builder()
                .token("delete-by-id-token")
                .userId(4L)
                .expiration(1800L)
                .build();
        tokenRepository.save(tokenEntity);

        // When: 토큰 ID로 엔티티를 삭제한다
        tokenRepository.deleteById("delete-by-id-token");

        // Then: 엔티티가 성공적으로 삭제된다
        Optional<TokenEntity> result = tokenRepository.findById("delete-by-id-token");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("동일한 토큰 ID로 기존 엔티티를 업데이트할 수 있다")
    void shouldUpdateExistingTokenEntity() {
        // Given: 기존 토큰 엔티티가 저장되어 있다
        TokenEntity originalEntity = TokenEntity.builder()
                .token("update-test-token")
                .userId(5L)
                .expiration(3600L)
                .build();
        tokenRepository.save(originalEntity);

        // When: 같은 토큰 ID로 새로운 정보를 가진 엔티티를 저장한다
        TokenEntity updatedEntity = TokenEntity.builder()
                .token("update-test-token")
                .userId(6L)
                .expiration(7200L)
                .build();
        tokenRepository.save(updatedEntity);

        // Then: 엔티티가 업데이트된다
        Optional<TokenEntity> result = tokenRepository.findById("update-test-token");
        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(6L);
        assertThat(result.get().getExpiration()).isEqualTo(7200L);
    }

    @Test
    @DisplayName("여러 TokenEntity를 저장하고 각각 개별적으로 조회할 수 있다")
    void shouldSaveAndRetrieveMultipleTokenEntities() {
        // Given: 여러 토큰 엔티티가 준비되어 있다
        TokenEntity entity1 = TokenEntity.builder()
                .token("token-1")
                .userId(7L)
                .expiration(3600L)
                .build();
        
        TokenEntity entity2 = TokenEntity.builder()
                .token("token-2")
                .userId(8L)
                .expiration(7200L)
                .build();

        // When: 여러 엔티티를 저장한다
        tokenRepository.save(entity1);
        tokenRepository.save(entity2);

        // Then: 각 엔티티를 개별적으로 조회할 수 있다
        Optional<TokenEntity> result1 = tokenRepository.findById("token-1");
        Optional<TokenEntity> result2 = tokenRepository.findById("token-2");
        
        assertThat(result1).isPresent();
        assertThat(result1.get().getUserId()).isEqualTo(7L);
        assertThat(result1.get().getExpiration()).isEqualTo(3600L);
        
        assertThat(result2).isPresent();
        assertThat(result2.get().getUserId()).isEqualTo(8L);
        assertThat(result2.get().getExpiration()).isEqualTo(7200L);
    }

    @Test
    @DisplayName("Repository에 존재하는 모든 엔티티 개수를 확인할 수 있다")
    void shouldCountAllTokenEntities() {
        // Given: 여러 토큰 엔티티가 저장되어 있다
        TokenEntity entity1 = TokenEntity.builder()
                .token("count-token-1")
                .userId(10L)
                .expiration(3600L)
                .build();
        
        TokenEntity entity2 = TokenEntity.builder()
                .token("count-token-2")
                .userId(11L)
                .expiration(7200L)
                .build();

        tokenRepository.save(entity1);
        tokenRepository.save(entity2);

        // When: 전체 엔티티 개수를 조회한다
        long count = tokenRepository.count();

        // Then: 저장된 엔티티 개수가 정확히 반환된다
        assertThat(count).isGreaterThanOrEqualTo(2);
    }
}