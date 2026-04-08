package madp.auth.domain.domain.repository;

import madp.auth.domain.domain.entity.TestUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestUserRepository extends JpaRepository<TestUserEntity, String> {
}
