package kr.santanrudolph.everyvent.domain.follow.command;


import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;

public record FollowCreateCommand(
    Long followerId,
    Long targetId
) {

  public FollowCreateCommand {
    validateNotNull(followerId, "팔로워 유저의 ID");
    validateNotNull(targetId, "팔로우 대상 유저의 ID");

    validatePositive(followerId, "팔로워 유저의 ID");
    validatePositive(targetId, "팔로우 대상 유저의 ID");

    validateNotSelfFollow(followerId, targetId);
  }

  private void validateNotNull(Long id, String fieldName) {
    if (id == null) {
      throw new EveryventException(
          ErrorCode.INVALID_INPUT,
          fieldName + "는 null일 수 없습니다."
      );
    }
  }

  private void validatePositive(Long id, String fieldName) {
    if (id <= 0) {
      throw new EveryventException(
          ErrorCode.INVALID_INPUT,
          fieldName + "는 1 이상의 값이어야 합니다."
      );
    }
  }

  private void validateNotSelfFollow(Long followerId, Long targetId) {
    if (followerId.equals(targetId)) {
      throw new EveryventException(
          ErrorCode.INVALID_INPUT,
          "자기 자신을 팔로우할 수 없습니다."
      );
    }
  }
}
