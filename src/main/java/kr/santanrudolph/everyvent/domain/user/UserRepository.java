package kr.santanrudolph.everyvent.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findById(Long id);

  boolean existsByNickname(String nickname);

  Optional<User> findByIdAndDeletedAtIsNull(Long id);

  Optional<User> findBySocialIdAndProviderAndDeletedAtIsNull(String socialId, String provider);

  Optional<User> findByEmail(String email);

}
