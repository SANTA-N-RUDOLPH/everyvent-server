package kr.santanrudolph.everyvent.domain.follow;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.lang.reflect.Field;
import java.util.Optional;
import kr.santanrudolph.everyvent.domain.follow.dto.FollowCreateCommand;
import kr.santanrudolph.everyvent.domain.follow.dto.FollowCreateResponse;
import kr.santanrudolph.everyvent.domain.user.SocialProvider;
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
  class CreateFollowTest{
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

      given(userRepository.findByIdAndDeletedAtIsNull(FOLLOWER_ID)).willReturn(Optional.of(follower));
      given(userRepository.findByIdAndDeletedAtIsNull(TARGET_ID)).willReturn(Optional.of(target));
      given(followRepository.existsByFollowerIdAndTargetId(FOLLOWER_ID, TARGET_ID)).willReturn(false);
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

      then(followRepository).should(times(1)).existsByFollowerIdAndTargetId(FOLLOWER_ID, TARGET_ID);
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
      given(userRepository.findByIdAndDeletedAtIsNull(FOLLOWER_ID)).willReturn(Optional.of(follower));
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
      given(userRepository.findByIdAndDeletedAtIsNull(FOLLOWER_ID)).willReturn(Optional.of(follower));
      given(userRepository.findByIdAndDeletedAtIsNull(TARGET_ID)).willReturn(Optional.of(target));
      given(followRepository.existsByFollowerIdAndTargetId(FOLLOWER_ID, TARGET_ID)).willReturn(true);

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

      then(followRepository).should(times(1)).existsByFollowerIdAndTargetId(FOLLOWER_ID, TARGET_ID);
      then(followRepository).should(never()).save(any(Follow.class));
    }
  }



}