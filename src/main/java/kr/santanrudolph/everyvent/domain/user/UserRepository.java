package kr.santanrudolph.everyvent.domain.user;

import kr.santanrudolph.everyvent.domain.user.enums.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  boolean existsByNickname(String nickname);

  Optional<User> findByIdAndDeletedAtIsNull(Long id);

  Optional<User> findBySocialIdAndSocialProviderAndDeletedAtIsNull(String socialId, SocialProvider socialProvider);

  Optional<User> findByEmail(String email);

  boolean existsByIdAndDeletedAtIsNull(Long Id);
}
