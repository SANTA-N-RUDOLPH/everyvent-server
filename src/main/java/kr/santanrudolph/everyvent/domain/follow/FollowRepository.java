package kr.santanrudolph.everyvent.domain.follow;

import java.util.List;
import kr.santanrudolph.everyvent.domain.follow.dto.FollowResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

  boolean existsByFollowerIdAndTargetId(Long followerId, Long targetId);

  @Query("SELECT  new kr.santanrudolph.everyvent.domain.follow.dto.FollowResponse(f.id, u.id, u.nickname)" +
      "FROM Follow f " +
      "JOIN FETCH f.follower u " +
      "WHERE f.target.id = :targetId AND u.deletedAt IS NULL")
  List<FollowResponse> findActiveFollowersByTargetId(@Param("targetId") Long targetId);

  @Query("SELECT new kr.santanrudolph.everyvent.domain.follow.dto.FollowResponse(f.id, u.id, u.nickname) " +
      "FROM Follow f " +
      "JOIN f.target u " +
      "WHERE f.follower.id = :followerId AND u.deletedAt IS NULL")
  List<FollowResponse> findActiveFollowingsBasicByFollowerId(@Param("followerId") Long followerId);

}
