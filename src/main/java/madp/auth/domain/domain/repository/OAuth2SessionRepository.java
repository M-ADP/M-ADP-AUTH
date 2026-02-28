package madp.auth.domain.domain.repository;

import madp.auth.domain.domain.entity.OAuth2SessionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OAuth2SessionRepository extends CrudRepository<OAuth2SessionEntity, String> {
}