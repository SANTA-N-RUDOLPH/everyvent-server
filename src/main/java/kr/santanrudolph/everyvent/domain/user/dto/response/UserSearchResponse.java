package kr.santanrudolph.everyvent.domain.user.dto.response;


import kr.santanrudolph.everyvent.domain.follow.FollowStatus;
import kr.santanrudolph.everyvent.domain.user.User;

import java.util.List;

public record UserSearchResponse(
    Long userId,
    String nickname,
    String profileImageKey,
    FollowStatus followStatus,
    List<UserBasicResponse> commonFollowers,   // 최대 3명
    long commonFollowersCount,                 // 전체 개수
    double score                               // 정렬 기준 점수
) {
  public static UserSearchResponse of(
      User user,
      FollowStatus status,
      List<UserBasicResponse> commonFollowers,
      long commonFollowersCount,
      double score
  ) {
    return new UserSearchResponse(
        user.getId(),
        user.getNickname(),
        user.getProfileImageKey(),
        status,
        commonFollowers,
        commonFollowersCount,
        score
    );
  }
}
