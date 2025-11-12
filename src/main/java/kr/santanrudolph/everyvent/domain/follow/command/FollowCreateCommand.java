package kr.santanrudolph.everyvent.domain.follow.command;


import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;

public record FollowCreateCommand(
    Long followerId,
    Long targetId
) {

  public FollowCreateCommand {
    if (followerId <= 0) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "팔로워 유저의 ID는 1 이상의 값이어야 합니다.");
    } else if (targetId <= 0) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "팔로우 대상 유저의 ID는 1 이상의 값이어야 합니다.");
    }
  }
}
