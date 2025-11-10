package kr.santanrudolph.everyvent.domain.follow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import java.util.List;
import kr.santanrudolph.everyvent.domain.user.SocialProvider;
import kr.santanrudolph.everyvent.domain.user.User;
import kr.santanrudolph.everyvent.domain.user.UserRepository;
import kr.santanrudolph.everyvent.domain.user.dto.response.UserBasicResponse;
import kr.santanrudolph.everyvent.global.config.JpaConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

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

    @DisplayName("팔로워의 id와 닉네임 목록을 조회한다.")
    @Test
    public void findFollowers() {
      // given
      User target = createAndSaveUser("target");
      User follower1 = createAndSaveUser("follower1");
      User follower2 = createAndSaveUser("follower2");

      Follow follow1 = createAndSaveFollow(follower1, target);
      Follow follow2 = createAndSaveFollow(follower2, target);

      // when
      List<UserBasicResponse> result = followRepository.findActiveFollowersBasicByTargetId(
          target.getId());

      // then
      assertThat(result).hasSize(2)
          .extracting("id", "nickname")
          .containsExactlyInAnyOrder(
              tuple(follower1.getId(), "follower1"),
              tuple(follower2.getId(), "follower2"));
    }

    @DisplayName("팔로워가 없으면 빈 리스트를 반환한다.")
    @Test
    public void findFollowers_whenNoFollowers_returnsEmptyList() {
      // given
      User target = createAndSaveUser("target");

      // when
      List<UserBasicResponse> result = followRepository.findActiveFollowersBasicByTargetId(
          target.getId());

      // then
      assertThat(result).isEmpty();

    }

    @DisplayName("탈퇴하지 않은 팔로워와의 관계만 반환한다.")
    @Test
    public void findFollowers_whenFollowerDeleted_thenReturnsOnlyActiveFollowers() {
      // given
      User target = createAndSaveUser("target");
      User activeUser = createAndSaveUser("activeUser");
      User deletedUser = createAndSaveDeletedUser("deletedUser");

      Follow follow1 = createAndSaveFollow(activeUser, target);
      Follow follow2 = createAndSaveFollow(deletedUser, target);

      // when
      List<UserBasicResponse> result = followRepository.findActiveFollowersBasicByTargetId(
          target.getId());

      // then
      assertThat(result).hasSize(1)
          .extracting("nickname")
          .containsExactly("activeUser");

    }

  }


}