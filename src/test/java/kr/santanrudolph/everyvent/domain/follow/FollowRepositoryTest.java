package kr.santanrudolph.everyvent.domain.follow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import java.util.List;

import kr.santanrudolph.everyvent.domain.follow.dto.FollowResponse;
import kr.santanrudolph.everyvent.domain.user.enums.SocialProvider;
import kr.santanrudolph.everyvent.domain.user.User;
import kr.santanrudolph.everyvent.domain.user.UserRepository;
import kr.santanrudolph.everyvent.global.config.JpaConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

@Tag("unit")
@DataJpaTest
@DisplayName("FollowRepository 테스트")
@Import(JpaConfig.class) // DataJpaTest는 JPA 관련 빈만 로딩한다. Config 빈은 따로 임포트 필요
class FollowRepositoryTest {

  @Autowired
  private FollowRepository followRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private TestEntityManager entityManager;

  // helper methods
  private User createAndSaveUser(String nickname) {
    User user = User.createFromOAuth(
        "social_id_" + nickname,
        SocialProvider.KAKAO,
        nickname + "@test.com",
        nickname
    );
    return userRepository.save(user);
  }

  private User createAndSaveDeletedUser(String nickname) {
    User user = User.createFromOAuth(
        "social_id_" + nickname,
        SocialProvider.KAKAO,
        nickname + "@test.com",
        nickname
    );
    user.softDelete();
    return userRepository.save(user);
  }

  private Follow createAndSaveFollow(User follower, User target) {
    Follow follow = Follow.create(follower, target);
    entityManager.persist(follow);
    entityManager.flush();
    return follow;
  }

  @Nested
  @DisplayName("팔로워 목록 조회 테스트")
  class FindFollowersTest {

    @DisplayName("follow_id, follower_id, follower_nickname 목록을 조회한다.")
    @Test
    void findFollowers_Success() {
      // given
      User target = createAndSaveUser("target");
      User follower1 = createAndSaveUser("follower1");
      User follower2 = createAndSaveUser("follower2");

      Follow follow1 = createAndSaveFollow(follower1, target);
      Follow follow2 = createAndSaveFollow(follower2, target);

      // when
      List<FollowResponse> result = followRepository.findActiveFollowersByTargetId(
          target.getId());

      // then
      assertThat(result).hasSize(2)
          .extracting("id", "user.id", "user.nickname")
          .containsExactlyInAnyOrder(
              tuple(follow1.getId(), follower1.getId(), "follower1"),
              tuple(follow2.getId(), follower2.getId(), "follower2"));
    }

    @DisplayName("팔로워가 없으면 빈 리스트를 반환한다.")
    @Test
    void findFollowers_whenNoFollowers_returnsEmptyList() {
      // given
      User target = createAndSaveUser("target");

      // when
      List<FollowResponse> result = followRepository.findActiveFollowersByTargetId(
          target.getId());

      // then
      assertThat(result).isEmpty();
    }

    @DisplayName("탈퇴하지 않은 팔로워와의 관계만 반환한다.")
    @Test
    void findFollowers_whenFollowerDeleted_thenReturnsOnlyActiveFollowers() {
      // given
      User target = createAndSaveUser("target");
      User activeUser = createAndSaveUser("activeUser");
      User deletedUser = createAndSaveDeletedUser("deletedUser");

      Follow follow1 = createAndSaveFollow(activeUser, target);
      Follow follow2 = createAndSaveFollow(deletedUser, target);

      // when
      List<FollowResponse> result = followRepository.findActiveFollowersByTargetId(
          target.getId());

      // then
      assertThat(result).hasSize(1)
          .extracting("id", "user.id", "user.nickname")
          .containsExactly(tuple(follow1.getId(), activeUser.getId(), activeUser.getNickname()));

    }

  }

  @Nested
  @DisplayName("팔로잉 목록 조회 테스트")
  class FindFollowingsTest {

    @DisplayName("follow_id, target_id, target_nickname 목록을 조회한다.")
    @Test
    void findFollowings_Success() {
      // given
      User follower = createAndSaveUser("follower");
      User target1 = createAndSaveUser("target1");
      User target2 = createAndSaveUser("target2");

      Follow follow1 = createAndSaveFollow(follower, target1);
      Follow follow2 = createAndSaveFollow(follower, target2);

      // when
      List<FollowResponse> result = followRepository.findActiveFollowingsByFollowerId(
          follower.getId());

      // then
      assertThat(result).hasSize(2)
          .extracting("id", "user.id", "user.nickname")
          .containsExactlyInAnyOrder(
              tuple(follow1.getId(), target1.getId(), target1.getNickname()),
              tuple(follow2.getId(), target2.getId(), target2.getNickname()));
    }

    @DisplayName("팔로잉 하는 사람이 없으면 빈 리스트를 반환한다.")
    @Test
    void findFollowings_whenNoFollowings_thenReturnsEmptyList() {
      // given
      User follower = createAndSaveUser("follower");

      // when
      List<FollowResponse> result = followRepository.findActiveFollowingsByFollowerId(
          follower.getId());

      // then
      assertThat(result).isEmpty();
    }

    @DisplayName("탈퇴하지 않은 팔로잉 대상과의 관계만 반환한다.")
    @Test
    void findFollowings_whenFollowingDeleted_thenReturnsOnlyActiveFollowings() {
      // given
      User follower = createAndSaveUser("follower");
      User target1 = createAndSaveUser("target1");
      User target2 = createAndSaveDeletedUser("target2");

      Follow follow1 = createAndSaveFollow(follower, target1);
      Follow follow2 = createAndSaveFollow(follower, target2);

      // when
      List<FollowResponse> result = followRepository.findActiveFollowingsByFollowerId(
          follower.getId());

      // then
      assertThat(result).hasSize(1)
          .extracting("id", "user.id", "user.nickname")
          .containsExactly(tuple(follow1.getId(), target1.getId(), target1.getNickname()));
    }

  }

  @Nested
  @DisplayName("팔로우 카운트 조회 테스트")
  class CountFollowsTest {

    @DisplayName("특정 사용자의 팔로워 수를 조회한다.")
    @Test
    void countActiveFollowers_Success() {
      // given
      User target = createAndSaveUser("target");
      User follower1 = createAndSaveUser("follower1");
      User follower2 = createAndSaveUser("follower2");

      createAndSaveFollow(follower1, target);
      createAndSaveFollow(follower2, target);

      // when
      int count = followRepository.countActiveFollowersByTargetId(target.getId());

      // then
      assertThat(count).isEqualTo(2);
    }

    @DisplayName("삭제된 유저의 팔로우는 팔로워 수에서 제외된다.")
    @Test
    void countActiveFollowers_whenFollowerDeleted_thenExcludesDeletedUser() {
      // given
      User target = createAndSaveUser("target");
      User activeFollower = createAndSaveUser("activeFollower");
      User deletedFollower = createAndSaveDeletedUser("deletedFollower");

      createAndSaveFollow(activeFollower, target);
      createAndSaveFollow(deletedFollower, target);

      // when
      int count = followRepository.countActiveFollowersByTargetId(target.getId());

      // then
      assertThat(count).isEqualTo(1);
    }

    @DisplayName("팔로워가 없으면 0을 반환한다.")
    @Test
    void countActiveFollowers_whenNoFollowers_thenReturnsZero() {
      // given
      User target = createAndSaveUser("target");

      // when
      int count = followRepository.countActiveFollowersByTargetId(target.getId());

      // then
      assertThat(count).isZero();
    }

    @DisplayName("특정 사용자의 팔로잉 수를 조회한다.")
    @Test
    void countActiveFollowings_Success() {
      // given
      User follower = createAndSaveUser("follower");
      User target1 = createAndSaveUser("target1");
      User target2 = createAndSaveUser("target2");

      createAndSaveFollow(follower, target1);
      createAndSaveFollow(follower, target2);

      // when
      int count = followRepository.countActiveFollowingsByFollowerId(follower.getId());

      // then
      assertThat(count).isEqualTo(2);
    }

    @DisplayName("삭제된 유저를 향한 팔로우는 팔로잉 수에서 제외된다.")
    @Test
    void countActiveFollowings_whenTargetDeleted_thenExcludesDeletedUser() {
      // given
      User follower = createAndSaveUser("follower");
      User activeTarget = createAndSaveUser("activeTarget");
      User deletedTarget = createAndSaveDeletedUser("deletedTarget");

      createAndSaveFollow(follower, activeTarget);
      createAndSaveFollow(follower, deletedTarget);

      // when
      int count = followRepository.countActiveFollowingsByFollowerId(follower.getId());

      // then
      assertThat(count).isEqualTo(1);
    }

    @DisplayName("팔로잉이 없으면 0을 반환한다.")
    @Test
    void countActiveFollowings_whenNoFollowings_thenReturnsZero() {
      // given
      User follower = createAndSaveUser("follower");

      // when
      int count = followRepository.countActiveFollowingsByFollowerId(follower.getId());

      // then
      assertThat(count).isZero();
    }

  }

  @Nested
  @DisplayName("사용자 탈퇴 시 팔로우 삭제 테스트")
  class DeleteAllByUserIdTest {

    @DisplayName("사용자가 팔로워인 모든 팔로우 관계를 삭제한다.")
    @Test
    void deleteAllByUserId_whenUserIsFollower_thenDeletesAll() {
      // given
      User follower = createAndSaveUser("follower");
      User target1 = createAndSaveUser("target1");
      User target2 = createAndSaveUser("target2");

      createAndSaveFollow(follower, target1);
      createAndSaveFollow(follower, target2);

      entityManager.flush();
      entityManager.clear();

      // when
      int deletedCount = followRepository.deleteAllByUserId(follower.getId());

      // then
      assertThat(deletedCount).isEqualTo(2);
      assertThat(followRepository.findAll()).isEmpty();
    }

    @DisplayName("사용자가 타겟인 모든 팔로우 관계를 삭제한다.")
    @Test
    void deleteAllByUserId_whenUserIsTarget_thenDeletesAll() {
      // given
      User target = createAndSaveUser("target");
      User follower1 = createAndSaveUser("follower1");
      User follower2 = createAndSaveUser("follower2");

      createAndSaveFollow(follower1, target);
      createAndSaveFollow(follower2, target);

      entityManager.flush();
      entityManager.clear();

      // when
      int deletedCount = followRepository.deleteAllByUserId(target.getId());

      // then
      assertThat(deletedCount).isEqualTo(2);
      assertThat(followRepository.findAll()).isEmpty();
    }

    @DisplayName("사용자가 팔로워이면서 동시에 타겟인 모든 관계를 삭제한다.")
    @Test
    void deleteAllByUserId_whenUserIsBothFollowerAndTarget_thenDeletesAll() {
      // given
      User user = createAndSaveUser("user");
      User other1 = createAndSaveUser("other1");
      User other2 = createAndSaveUser("other2");

      createAndSaveFollow(user, other1);
      createAndSaveFollow(other2, user);

      entityManager.flush();
      entityManager.clear();

      // when
      int deletedCount = followRepository.deleteAllByUserId(user.getId());

      // then
      assertThat(deletedCount).isEqualTo(2);
      assertThat(followRepository.findAll()).isEmpty();
    }

    @DisplayName("팔로우 관계가 없으면 0을 반환한다.")
    @Test
    void deleteAllByUserId_whenNoFollows_thenReturnsZero() {
      // given
      User user = createAndSaveUser("user");

      // when
      int deletedCount = followRepository.deleteAllByUserId(user.getId());

      // then
      assertThat(deletedCount).isZero();
    }

  }

}
