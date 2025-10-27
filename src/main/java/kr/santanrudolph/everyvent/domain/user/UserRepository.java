package kr.santanrudolph.everyvent.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findById(Long id);

  boolean existsByNickname(String nickname);

  Optional<User> findByIdAndDeletedAtIsNull(Long id);

  Optional<User> findBySocialIdAndSocialProviderAndDeletedAtIsNull(String socialId, SocialProvider socialProvider);

  Optional<User> findByEmail(String email);

  List<User> findAllByRoleAndDeletedAtIsNull(Role role);
}
