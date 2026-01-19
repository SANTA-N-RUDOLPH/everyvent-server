package kr.santanrudolph.everyvent.domain.user.repository;


import kr.santanrudolph.everyvent.domain.user.User;
import kr.santanrudolph.everyvent.domain.user.enums.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {

  boolean existsByNickname(String nickname);

  Optional<User> findByIdAndDeletedAtIsNull(Long id);

  Optional<User> findBySocialIdAndSocialProviderAndDeletedAtIsNull(String socialId, SocialProvider socialProvider);

  Optional<User> findByEmail(String email);

  boolean existsByIdAndDeletedAtIsNull(Long Id);

  @Query(
      value = """
          SELECT
            t.target_user_id AS targetUserId,
            u.id AS userId,
            u.nickname,
            u.profile_image_key AS profileImageKey
          FROM (
            SELECT
              f1.target_id AS target_user_id,
              f1.follower_id,
              ROW_NUMBER() OVER (
                PARTITION BY f1.target_id
                ORDER BY f1.follower_id
              ) AS rn
            FROM follow f1
            JOIN follow f2
              ON f2.target_id = f1.follower_id
             AND f2.follower_id = :currentUserId
            WHERE f1.target_id IN (:targetUserIds)
          ) t
          JOIN user u ON u.id = t.follower_id
          WHERE t.rn <= 3
            AND u.deleted_at IS NULL
          """,
      nativeQuery = true
  )
  List<CommonFollowerProjection> findCommonFollowersBatch(
      @Param("currentUserId") Long currentUserId,
      @Param("targetUserIds") List<Long> targetUserIds
  );


}
