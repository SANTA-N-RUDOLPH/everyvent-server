package kr.santanrudolph.everyvent.domain.user;

import kr.santanrudolph.everyvent.domain.user.enums.Role;
import kr.santanrudolph.everyvent.domain.user.enums.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  boolean existsByNickname(String nickname);

  Optional<User> findByIdAndDeletedAtIsNull(Long id);

  Optional<User> findBySocialIdAndSocialProviderAndDeletedAtIsNull(String socialId, SocialProvider socialProvider);

  Optional<User> findByEmail(String email);

  boolean existsByIdAndDeletedAtIsNull(Long Id);

  @Query("""
    SELECT u FROM User u
    WHERE u.role = :role AND u.deletedAt IS NULL
    AND u.id NOT IN (
        SELECT c.user.id FROM DistributedCalendar c
        WHERE c.originalCalendar.id = :originalId
    )
   """)
  List<User> findTargetUsersForDistribution(@Param("role") Role role, @Param("originalId") Long originalId);
}
