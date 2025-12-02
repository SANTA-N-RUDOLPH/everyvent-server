package kr.santanrudolph.everyvent.domain.follow;

import java.util.List;

import kr.santanrudolph.everyvent.domain.follow.dto.FollowResponse;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

  boolean existsByFollowerIdAndTargetId(Long followerId, Long targetId);

  @Query("SELECT COUNT(f) FROM Follow f " +
      "WHERE f.target.id = :targetId " +
      "AND f.follower.deletedAt IS NULL ")
  int countActiveFollowersByTargetId(@Param("targetId") Long targetId);

  @Query("SELECT COUNT(f) FROM Follow f " +
      "WHERE f.follower.id = :followerId " +
      "AND f.target.deletedAt IS NULL")
  int countActiveFollowingsByFollowerId(@Param("followerId") Long followerId);

  @Query(
      "SELECT new kr.santanrudolph.everyvent.domain.follow.dto.FollowResponse(f.id, new kr.santanrudolph.everyvent.domain.user.dto.response.UserBasicResponse(u.id, u.nickname, u.introduction)) "
          +
          "FROM Follow f " +
          "JOIN f.follower u " +
          "WHERE f.target.id = :targetId AND u.deletedAt IS NULL")
  List<FollowResponse> findActiveFollowersByTargetId(@Param("targetId") Long targetId);

  @Query(
      "SELECT new kr.santanrudolph.everyvent.domain.follow.dto.FollowResponse(f.id, new kr.santanrudolph.everyvent.domain.user.dto.response.UserBasicResponse(u.id, u.nickname, u.introduction)) "
          +
          "FROM Follow f " +
          "JOIN f.target u " +
          "WHERE f.follower.id = :followerId AND u.deletedAt IS NULL")
  List<FollowResponse> findActiveFollowingsByFollowerId(@Param("followerId") Long followerId);

  Optional<Follow> findByFollowerIdAndTargetId(Long followerId, Long targetId);

  @Modifying
  @Query("DELETE FROM Follow f WHERE f.follower.id = :followerId AND f.target.id = :targetId")
  int deleteByFollowerIdAndTargetId(@Param("followerId") Long followerId,
                                    @Param("targetId") Long targetId);

  @Modifying
  @Query("DELETE FROM Follow f WHERE f.follower.id = :userId OR f.target.id = :userId")
  int deleteAllByUserId(@Param("userId") Long userId);
}
