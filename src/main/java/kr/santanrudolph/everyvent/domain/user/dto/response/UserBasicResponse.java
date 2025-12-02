package kr.santanrudolph.everyvent.domain.user.dto.response;


import kr.santanrudolph.everyvent.domain.user.User;

public record UserBasicResponse(

    Long id,
    String nickname,
    String introduction
) {

  public static UserBasicResponse from(User user) {
    return new UserBasicResponse(
        user.getId(),
        user.getNickname(),
        user.getIntroduction()
    );
  }
}
