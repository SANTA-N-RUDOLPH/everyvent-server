package kr.santanrudolph.everyvent.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.util.Optional;
import kr.santanrudolph.everyvent.domain.follow.FollowRepository;
import kr.santanrudolph.everyvent.domain.user.enums.SocialProvider;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private FollowRepository followRepository;

  @InjectMocks
  private UserService userService;

  // helper method
  private User createUser(Long id, String nickname) {
    User user = User.createFromOAuth(
        "social_id_" + id,
        SocialProvider.KAKAO,
        nickname + "@test.com",
        nickname
    );

    try {
      FieldUtils.writeField(user, "id", id, true);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("테스트용 User id 설정 실패", e);
    }

    return user;
  }

  @Nested
  @DisplayName("회원 탈퇴 테스트")
  class DeleteUserTest {

    @DisplayName("회원 탈퇴 시 User는 soft delete되고 Follow는 hard delete된다.")
    @Test
    void deleteUser_Success() {
      // given
      Long userId = 1L;
      User user = createUser(userId, "user");

      given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));
      given(followRepository.deleteAllByUserId(userId)).willReturn(5);

      // when
      userService.deleteUser(userId);

      // then
      assertThat(user.isDeleted()).isTrue();
      assertThat(user.getDeletedAt()).isNotNull();

      then(userRepository).should(times(1)).findByIdAndDeletedAtIsNull(userId);
      then(followRepository).should(times(1)).deleteAllByUserId(userId);
    }

    @DisplayName("사용자를 찾을 수 없으면 예외를 던진다.")
    @Test
    void deleteUser_whenUserNotFound_thenThrowsException() {
      // given
      Long userId = 1L;
      given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> userService.deleteUser(userId))
          .isInstanceOf(EveryventException.class)
          .extracting("errorCode", "detail")
          .containsExactly(
              ErrorCode.NOT_FOUND,
              "탈퇴할 사용자를 찾을 수 없습니다."
          );

      then(userRepository).should(times(1)).findByIdAndDeletedAtIsNull(userId);
      then(followRepository).should(times(0)).deleteAllByUserId(userId);
    }

    @DisplayName("팔로우 관계가 없어도 탈퇴는 정상 처리된다.")
    @Test
    void deleteUser_whenNoFollows_thenStillSucceeds() {
      // given
      Long userId = 1L;
      User user = createUser(userId, "user");

      given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));
      given(followRepository.deleteAllByUserId(userId)).willReturn(0);

      // when
      userService.deleteUser(userId);

      // then
      assertThat(user.isDeleted()).isTrue();

      then(userRepository).should(times(1)).findByIdAndDeletedAtIsNull(userId);
      then(followRepository).should(times(1)).deleteAllByUserId(userId);
    }

  }
}
