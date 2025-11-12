package kr.santanrudolph.everyvent.domain.follow;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import kr.santanrudolph.everyvent.domain.follow.command.FollowCreateCommand;
import kr.santanrudolph.everyvent.domain.follow.dto.FollowCreateResponse;
import kr.santanrudolph.everyvent.domain.follow.dto.FollowResponse;
import kr.santanrudolph.everyvent.domain.user.enums.SocialProvider;
import kr.santanrudolph.everyvent.domain.user.User;
import kr.santanrudolph.everyvent.domain.user.UserRepository;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("FollowService 단위 테스트")
class FollowServiceTest {

  @Mock
  private FollowRepository followRepository;
  @Mock
  private UserRepository userRepository;
  @InjectMocks
  private FollowService followService;

  // reflection field
  private static final Field USER_ID_FIELD;
  private static final Field FOLLOW_ID_FIELD;

  static {
    USER_ID_FIELD = FieldUtils.getField(User.class, "id", true);
    USER_ID_FIELD.setAccessible(true);
    FOLLOW_ID_FIELD = FieldUtils.getField(Follow.class, "id", true);
    FOLLOW_ID_FIELD.setAccessible(true);
  }

  private static final Long FOLLOWER_ID = 1L;
  private static final Long TARGET_ID = 2L;
  private static final Long FOLLOW_ID = 3L;

  // Test fixtures
  private FollowCreateCommand command;
  private User follower;
  private User target;


  // helper method
  private User createUser(Long id, String nickname) {
    User user = User.createFromOAuth(
        "test_socail_id_" + id,
        SocialProvider.KAKAO,
        nickname + "@test.com",
        nickname
    );

    try {
      USER_ID_FIELD.set(user, id);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("테스트를 위한 유저 id 세팅에 실패했습니다.", e);
    }

    return user;
  }

  private Follow createFollow(User follower, User target, Long followId) {
    Follow follow = Follow.create(follower, target);
    try {
      FOLLOW_ID_FIELD.set(follow, followId);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("테스트용 Follow id 설정 실패", e);
    }
    return follow;
  }

  @Nested
  @DisplayName("Follow 관계 생성 테스트")
  class CreateFollowTest {

    @BeforeEach
    void setUp() {
      // 공통 테스트 픽스쳐 초기화
      command = new FollowCreateCommand(FOLLOWER_ID, TARGET_ID);

      follower = createUser(FOLLOWER_ID, "follower");
      target = createUser(TARGET_ID, "target");
    }

    @DisplayName("팔로우 관계를 생성한다.")
    @Test
    void createFollow_Success() {
      // given
      Follow expectedFollow = createFollow(follower, target, FOLLOW_ID);

      given(userRepository.findByIdAndDeletedAtIsNull(FOLLOWER_ID)).willReturn(Optional.of(
          follower));
      given(userRepository.findByIdAndDeletedAtIsNull(TARGET_ID)).willReturn(Optional.of(target));
      given(followRepository.existsByFollowerIdAndTargetId(FOLLOWER_ID, TARGET_ID)).willReturn(
          false);
      given(followRepository.save(any(Follow.class))).willReturn(expectedFollow);

      // when
      FollowCreateResponse response = followService.createFollow(command);

      // then
      assertThat(response).isNotNull();
      assertThat(response.followerId()).isEqualTo(FOLLOWER_ID);
      assertThat(response.targetId()).isEqualTo(TARGET_ID);
      assertThat(response.followId()).isEqualTo(FOLLOW_ID);

      then(userRepository).should(times(1)).findByIdAndDeletedAtIsNull(FOLLOWER_ID);
      then(userRepository).should(times(1)).findByIdAndDeletedAtIsNull(TARGET_ID);

      then(followRepository).should(times(1)).existsByFollowerIdAndTargetId(FOLLOWER_ID,
          TARGET_ID);
      ArgumentCaptor<Follow> followCaptor = ArgumentCaptor.forClass(Follow.class);
      then(followRepository).should(times(1)).save(followCaptor.capture());
      Follow savedFollow = followCaptor.getValue();
      assertThat(savedFollow).isNotNull();
      assertThat(savedFollow.getFollower()).isEqualTo(follower);
      assertThat(savedFollow.getTarget()).isEqualTo(target);
    }

    @DisplayName("팔로워를 찾을 수 없으면 예외를 던진다.")
    @Test
    void createFollow_whenFollowerNotFound_thenThrowsException() {
      // given
      given(userRepository.findByIdAndDeletedAtIsNull(FOLLOWER_ID)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> followService.createFollow(command))
          .isInstanceOf(EveryventException.class)
          .extracting("errorCode", "detail")
          .containsExactly(
              ErrorCode.NOT_FOUND,
              "팔로워 id를 찾을 수 없습니다."
          );

      then(userRepository).should(times(1)).findByIdAndDeletedAtIsNull(FOLLOWER_ID);
      then(userRepository).should(never()).findByIdAndDeletedAtIsNull(TARGET_ID);

      then(followRepository).should(never()).existsByFollowerIdAndTargetId(any(), any());
      then(followRepository).should(never()).save(any(Follow.class));
    }

    @DisplayName("팔로우 대상을 찾을 수 없으면 예외를 던진다.")
    @Test
    void createFollow_whenTargetNotFound_thenThrowsException() {
      // given
      given(userRepository.findByIdAndDeletedAtIsNull(FOLLOWER_ID)).willReturn(Optional.of(
          follower));
      given(userRepository.findByIdAndDeletedAtIsNull(TARGET_ID)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> followService.createFollow(command))
          .isInstanceOf(EveryventException.class)
          .extracting("errorCode", "detail")
          .containsExactly(
              ErrorCode.NOT_FOUND,
              "팔로우 대상 id를 찾을 수 없습니다."
          );

      then(userRepository).should(times(1)).findByIdAndDeletedAtIsNull(FOLLOWER_ID);
      then(userRepository).should(times(1)).findByIdAndDeletedAtIsNull(TARGET_ID);

      then(followRepository).should(never()).existsByFollowerIdAndTargetId(any(), any());
      then(followRepository).should(never()).save(any(Follow.class));
    }

    @DisplayName("이미 팔로우 중이면 예외를 던진다.")
    @Test
    void createFollow_whenFollowExists_thenThrowsException() {
      // given
      given(userRepository.findByIdAndDeletedAtIsNull(FOLLOWER_ID)).willReturn(Optional.of(
          follower));
      given(userRepository.findByIdAndDeletedAtIsNull(TARGET_ID)).willReturn(Optional.of(target));
      given(followRepository.existsByFollowerIdAndTargetId(FOLLOWER_ID, TARGET_ID)).willReturn(
          true);

      // when & then
      assertThatThrownBy(() -> followService.createFollow(command))
          .isInstanceOf(EveryventException.class)
          .extracting("errorCode", "detail")
          .containsExactly(
              ErrorCode.ALREADY_EXIST,
              "이미 팔로우 중입니다."
          );

      then(userRepository).should(times(1)).findByIdAndDeletedAtIsNull(FOLLOWER_ID);
      then(userRepository).should(times(1)).findByIdAndDeletedAtIsNull(TARGET_ID);

      then(followRepository).should(times(1)).existsByFollowerIdAndTargetId(FOLLOWER_ID,
          TARGET_ID);
      then(followRepository).should(never()).save(any(Follow.class));
    }
  }

  @Nested
  @DisplayName("팔로워 목록 조회 테스트")
  class GetFollowersTest {

    @DisplayName("TARGET_ID로 팔로워 목록을 조회한다.")
    @Test
    void getFollowers_Success() {
      // given
      Long FOLLOWER_ID2 = 4L;
      Long FOLLOW_ID2 = 5L;

      target = createUser(TARGET_ID, "target");

      List<FollowResponse> expectedFollowers = List.of(
          new FollowResponse(FOLLOW_ID, FOLLOWER_ID, "follower1"),
          new FollowResponse(FOLLOW_ID2, FOLLOWER_ID2, "follower2")
      );

      given(userRepository.findByIdAndDeletedAtIsNull(TARGET_ID)).willReturn(Optional.of(target));
      given(followRepository.findActiveFollowersByTargetId(TARGET_ID)).willReturn(
          expectedFollowers);

      // when
      List<FollowResponse> result = followService.getFollowers(TARGET_ID);

      // then
      assertThat(result)
          .hasSize(2)
          .extracting("id", "user.id", "user.nickname")
          .containsExactly(
              tuple(FOLLOW_ID, FOLLOWER_ID, "follower1"),
              tuple(FOLLOW_ID2, FOLLOWER_ID2, "follower2"));
      then(userRepository).should(times(1)).findByIdAndDeletedAtIsNull(TARGET_ID);
      then(followRepository).should(times(1)).findActiveFollowersByTargetId(TARGET_ID);
    }

    @DisplayName("팔로워가 없으면 빈 리스트를 반환한다.")
    @Test
    void getFollowers_whenNoFollowers_thenReturnsEmptyList() {
      // given
      target = createUser(TARGET_ID, "target");
      given(userRepository.findByIdAndDeletedAtIsNull(TARGET_ID)).willReturn(Optional.of(target));
      given(followRepository.findActiveFollowersByTargetId(TARGET_ID)).willReturn(List.of());

      // when
      List<FollowResponse> result = followService.getFollowers(TARGET_ID);

      // then
      assertThat(result).isEmpty();
      then(userRepository).should(times(1)).findByIdAndDeletedAtIsNull(TARGET_ID);
      then(followRepository).should(times(1)).findActiveFollowersByTargetId(TARGET_ID);
    }

    @DisplayName("팔로워 정보를 가져올 사용자가 존재하지 않으면 예외를 던진다.")
    @Test
    void getFollowers_whenTargetNotFound_thenThrowsException() {
      // given
      given(userRepository.findByIdAndDeletedAtIsNull(TARGET_ID)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> followService.getFollowers(TARGET_ID))
          .isInstanceOf(EveryventException.class)
          .extracting("errorCode", "detail")
          .containsExactly(ErrorCode.NOT_FOUND,
              "팔로워 정보를 가져올 대상 사용자가 존재하지 않습니다. (ID: " + TARGET_ID + ")");
      then(userRepository).should(times(1)).findByIdAndDeletedAtIsNull(TARGET_ID);
      then(followRepository).should(never()).findActiveFollowersByTargetId(any());
    }

  }

  @Nested
  @DisplayName("팔로잉 목록 조회 테스트")
  class GetFollowingsTest {

    @DisplayName("FOLLOWER_ID로 팔로잉 목록을 조회한다.")
    @Test
    void getFollowings_Success() {
      // given
      Long TARGET_ID2 = 4L;
      Long FOLLOW_ID2 = 5L;

      follower = createUser(FOLLOWER_ID, "follower");

      List<FollowResponse> expectedFollowings = List.of(
          new FollowResponse(FOLLOW_ID, TARGET_ID, "target1"),
          new FollowResponse(FOLLOW_ID2, TARGET_ID2, "target2")
      );

      given(userRepository.findByIdAndDeletedAtIsNull(FOLLOWER_ID)).willReturn(
          Optional.of(follower));
      given(followRepository.findActiveFollowingsByFollowerId(FOLLOWER_ID)).willReturn(
          expectedFollowings);

      // when
      List<FollowResponse> result = followService.getFollowings(FOLLOWER_ID);

      // then
      assertThat(result)
          .hasSize(2)
          .extracting("id", "user.id", "user.nickname")
          .containsExactly(
              tuple(FOLLOW_ID, TARGET_ID, "target1"),
              tuple(FOLLOW_ID2, TARGET_ID2, "target2"));
      then(userRepository).should(times(1)).findByIdAndDeletedAtIsNull(FOLLOWER_ID);
      then(followRepository).should(times(1)).findActiveFollowingsByFollowerId(FOLLOWER_ID);
    }

    @DisplayName("팔로잉하는 사람이 없으면 빈 리스트를 반환한다.")
    @Test
    void getFollowings_whenNoFollowings_thenReturnsEmptyList() {
      // given
      follower = createUser(FOLLOWER_ID, "follower");
      given(userRepository.findByIdAndDeletedAtIsNull(FOLLOWER_ID)).willReturn(
          Optional.of(follower));
      given(followRepository.findActiveFollowingsByFollowerId(FOLLOWER_ID)).willReturn(List.of());

      // when
      List<FollowResponse> result = followService.getFollowings(FOLLOWER_ID);

      // then
      assertThat(result).isEmpty();
      then(userRepository).should(times(1)).findByIdAndDeletedAtIsNull(FOLLOWER_ID);
      then(followRepository).should(times(1)).findActiveFollowingsByFollowerId(FOLLOWER_ID);
    }

    @DisplayName("팔로잉 정보를 가져올 사용자가 존재하지 않으면 예외를 던진다.")
    @Test
    void getFollowings_whenFollowerNotFound_thenThrowsException() {
      // given
      given(userRepository.findByIdAndDeletedAtIsNull(FOLLOWER_ID)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> followService.getFollowings(FOLLOWER_ID))
          .isInstanceOf(EveryventException.class)
          .extracting("errorCode", "detail")
          .containsExactly(ErrorCode.NOT_FOUND,
              "팔로잉 정보를 가져올 대상 사용자가 존재하지 않습니다. (ID: " + FOLLOWER_ID + ")"
          );
      then(userRepository).should(times(1)).findByIdAndDeletedAtIsNull(FOLLOWER_ID);
      then(followRepository).should(never()).findActiveFollowingsByFollowerId(any());
    }

  }

  @Nested
  @DisplayName("팔로우 관계 삭제 테스트")
  class DeleteFollowTest {

    @DisplayName("팔로우 관계를 삭제한다.")
    @Test
    void deleteFollow_Success() {
      // given
      given(userRepository.existsByIdAndDeletedAtIsNull(FOLLOWER_ID)).willReturn(true);
      given(userRepository.existsByIdAndDeletedAtIsNull(TARGET_ID)).willReturn(true);
      given(followRepository.deleteByFollowerIdAndTargetId(FOLLOWER_ID, TARGET_ID)).willReturn(1);

      // when
      followService.deleteFollow(FOLLOWER_ID, TARGET_ID);

      // then
      then(userRepository).should(times(1)).existsByIdAndDeletedAtIsNull(FOLLOWER_ID);
      then(userRepository).should(times(1)).existsByIdAndDeletedAtIsNull(TARGET_ID);
      then(followRepository).should(times(1)).deleteByFollowerIdAndTargetId(FOLLOWER_ID, TARGET_ID);
    }

    @DisplayName("팔로워 사용자가 존재하지 않으면 예외를 던진다.")
    @Test
    void deleteFollow_whenFollowerNotFound_throwsException() {
      // given
      given(userRepository.existsByIdAndDeletedAtIsNull(FOLLOWER_ID)).willReturn(false);

      // when & then
      assertThatThrownBy(() -> followService.deleteFollow(FOLLOWER_ID, TARGET_ID))
          .isInstanceOf(EveryventException.class)
          .extracting("errorCode", "detail")
          .containsExactly(
              ErrorCode.NOT_FOUND,
              "삭제할 팔로워 사용자를 찾을 수 없습니다. (ID: " + FOLLOWER_ID + ")"
          );

      then(userRepository).should(times(1)).existsByIdAndDeletedAtIsNull(FOLLOWER_ID);
      then(userRepository).should(never()).existsByIdAndDeletedAtIsNull(TARGET_ID);
      then(followRepository).should(never()).deleteByFollowerIdAndTargetId(any(), any());
    }

    @DisplayName("팔로우 대상 사용자가 존재하지 않으면 예외를 던진다.")
    @Test
    void deleteFollow_whenTargetNotFound_throwsException() {
      // given
      given(userRepository.existsByIdAndDeletedAtIsNull(FOLLOWER_ID)).willReturn(true);
      given(userRepository.existsByIdAndDeletedAtIsNull(TARGET_ID)).willReturn(false);

      // when & then
      assertThatThrownBy(() -> followService.deleteFollow(FOLLOWER_ID, TARGET_ID))
          .isInstanceOf(EveryventException.class)
          .extracting("errorCode", "detail")
          .containsExactly(
              ErrorCode.NOT_FOUND,
              "삭제할 팔로우 대상 사용자를 찾을 수 없습니다. (ID: " + TARGET_ID + ")"
          );

      then(userRepository).should(times(1)).existsByIdAndDeletedAtIsNull(FOLLOWER_ID);
      then(userRepository).should(times(1)).existsByIdAndDeletedAtIsNull(TARGET_ID);
      then(followRepository).should(never()).deleteByFollowerIdAndTargetId(any(), any());
    }

    @DisplayName("삭제할 팔로우 관계가 존재하지 않으면 예외를 던진다.")
    @Test
    void deleteFollow_whenFollowNotFound_throwsException() {
      // given
      given(userRepository.existsByIdAndDeletedAtIsNull(FOLLOWER_ID)).willReturn(true);
      given(userRepository.existsByIdAndDeletedAtIsNull(TARGET_ID)).willReturn(true);
      given(followRepository.deleteByFollowerIdAndTargetId(FOLLOWER_ID, TARGET_ID)).willReturn(0);

      // when & then
      assertThatThrownBy(() -> followService.deleteFollow(FOLLOWER_ID, TARGET_ID))
          .isInstanceOf(EveryventException.class)
          .extracting("errorCode", "detail")
          .containsExactly(
              ErrorCode.NOT_FOUND,
              "삭제할 팔로우 관계가 존재하지 않습니다."
          );

      then(userRepository).should(times(1)).existsByIdAndDeletedAtIsNull(FOLLOWER_ID);
      then(userRepository).should(times(1)).existsByIdAndDeletedAtIsNull(TARGET_ID);
      then(followRepository).should(times(1)).deleteByFollowerIdAndTargetId(FOLLOWER_ID, TARGET_ID);
    }

  }
}