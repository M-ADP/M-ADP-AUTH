package madp.auth.domain.domain.repository;

import madp.auth.domain.domain.entity.AuthCodeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthCodeRepository extends CrudRepository<AuthCodeEntity, String> {
    Optional<AuthCodeEntity> findByAuthCode(String authCode);
}
