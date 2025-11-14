package kr.santanrudolph.everyvent.domain.follow.dto;


import kr.santanrudolph.everyvent.domain.follow.Follow;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;

public record FollowCreateResponse(
    Long followId,
    Long followerId,
    Long targetId
) {

  public static FollowCreateResponse from(Follow follow) {
    if (follow == null) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "Follow 객체가 null입니다.");
    }

    return new FollowCreateResponse(
        follow.getId(),
        follow.getFollower().getId(),
        follow.getTarget().getId()
    );
  }
}
