package kr.santanrudolph.everyvent.domain.follow;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

  boolean existsByFollowerIdAndTargetId(Long followerId, Long targetId);
}
