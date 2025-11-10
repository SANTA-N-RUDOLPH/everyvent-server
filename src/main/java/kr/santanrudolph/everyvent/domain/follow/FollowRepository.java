package kr.santanrudolph.everyvent.domain.follow;

import java.util.List;
import kr.santanrudolph.everyvent.domain.user.dto.response.UserBasicResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

  boolean existsByFollowerIdAndTargetId(Long followerId, Long targetId);

  @Query("SELECT new kr.santanrudolph.everyvent.domain.user.dto.response.UserBasicResponse(u.id, u.nickname) " +
      "FROM Follow f " +
      "JOIN f.follower u " +
      "WHERE f.target.id = :targetId AND u.deletedAt IS NULL")
  List<UserBasicResponse> findFollowersBasicByTargetId(@Param("targetId") Long targetId);

  @Query("SELECT new kr.santanrudolph.everyvent.domain.user.dto.response.UserBasicResponse(u.id, u.nickname) " +
      "FROM Follow f " +
      "JOIN f.target u " +
      "WHERE f.follower.id = :followerId AND u.deletedAt IS NULL")
  List<UserBasicResponse> findFollowingsBasicByFollowerId(@Param("followerId") Long followerId);

}
