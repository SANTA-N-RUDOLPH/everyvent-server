package kr.santanrudolph.everyvent.auth.repository;

import java.time.Instant;
import kr.santanrudolph.everyvent.auth.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {

  boolean existsByToken(String token);

  void deleteByExpiresAtBefore(Instant now);
}
