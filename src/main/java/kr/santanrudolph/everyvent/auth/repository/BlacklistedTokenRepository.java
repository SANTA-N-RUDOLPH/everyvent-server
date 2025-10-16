package kr.santanrudolph.everyvent.auth.repository;

import kr.santanrudolph.everyvent.auth.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {

  boolean existsByToken(String token);

  void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
