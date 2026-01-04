package kr.santanrudolph.everyvent.domain.follow.dto;

import kr.santanrudolph.everyvent.domain.user.dto.response.UserBasicResponse;

public record FollowResponse(
    Long id,
    UserBasicResponse user
) {

  public FollowResponse(Long id, Long userId, String userNickname, String userIntroduction, String profileImageKey) {
    this(id, new UserBasicResponse(userId, userNickname, userIntroduction, profileImageKey));
  }
}
