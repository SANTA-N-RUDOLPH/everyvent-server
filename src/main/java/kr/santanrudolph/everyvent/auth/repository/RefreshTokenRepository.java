package kr.santanrudolph.everyvent.auth.repository;

import kr.santanrudolph.everyvent.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByToken(String token);

  void deleteByUserId(Long userId);

  boolean existsByToken(String token);
}
