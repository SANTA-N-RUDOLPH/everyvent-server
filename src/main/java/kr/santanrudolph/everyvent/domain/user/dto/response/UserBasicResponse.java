package kr.santanrudolph.everyvent.domain.user.dto.response;


import kr.santanrudolph.everyvent.domain.user.User;

public record UserBasicResponse(

    Long userId,
    String nickname,
    String profileImageKey,
    String introduction
) {

  public static UserBasicResponse from(User user) {
    return new UserBasicResponse(
        user.getId(),
        user.getNickname(),
        user.getProfileImageKey(),
        user.getIntroduction()
    );
  }
}
