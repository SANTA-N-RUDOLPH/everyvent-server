package kr.santanrudolph.everyvent.domain.follow;

import java.util.List;
import java.util.Optional;

import kr.santanrudolph.everyvent.domain.follow.dto.FollowResponse;
import kr.santanrudolph.everyvent.domain.follow.dto.FollowerCountProjection;
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
      "SELECT new kr.santanrudolph.everyvent.domain.follow.dto.FollowResponse(f.id, u.id, u.nickname, u.introduction, u.profileImageKey) "
          +
          "FROM Follow f " +
          "JOIN f.follower u " +
          "WHERE f.target.id = :targetId AND u.deletedAt IS NULL")
  List<FollowResponse> findActiveFollowersByTargetId(@Param("targetId") Long targetId);

  @Query(
      "SELECT new kr.santanrudolph.everyvent.domain.follow.dto.FollowResponse(f.id, u.id, u.nickname, u.introduction, u.profileImageKey) "
          +
          "FROM Follow f " +
          "JOIN f.target u " +
          "WHERE f.follower.id = :followerId AND u.deletedAt IS NULL")
  List<FollowResponse> findActiveFollowingsByFollowerId(@Param("followerId") Long followerId);

  // 내가 팔로우하는 대상들
  @Query("""
        select f.target.id
        from Follow f
        where f.follower.id = :me
          and f.target.id in :targets
      """)
  List<Long> findFollowingIds(
      @Param("me") Long me,
      @Param("targets") List<Long> targets
  );

  // 나를 팔로우하는 사람들
  @Query("""
        select f.follower.id
        from Follow f
        where f.target.id = :me
          and f.follower.id in :targets
      """)
  List<Long> findFollowerIds(
      @Param("me") Long me,
      @Param("targets") List<Long> targets
  );

  @Query("""
        select f.target.id as userId, count(f.id) as count
        from Follow f
        where f.target.id in :userIds
        and f.follower.deletedAt is null
        group by f.target.id
      """)
  List<FollowerCountProjection> countFollowersByUserIds(
      @Param("userIds") List<Long> userIds
  );

  @Modifying
  @Query("DELETE FROM Follow f WHERE f.follower.id = :followerId AND f.target.id = :targetId")
  int deleteByFollowerIdAndTargetId(@Param("followerId") Long followerId,
                                    @Param("targetId") Long targetId);

  @Modifying
  @Query("DELETE FROM Follow f WHERE f.follower.id = :userId OR f.target.id = :userId")
  int deleteAllByUserId(@Param("userId") Long userId);
}
