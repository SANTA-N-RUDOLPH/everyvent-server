package kr.santanrudolph.everyvent.domain.calendar.dto;

import kr.santanrudolph.everyvent.domain.user.User;

public record ScrapperResponse(
    Long id,
    String nickname,
    String introduction
) {

  public static ScrapperResponse from(User user) {
    return new ScrapperResponse(
        user.getId(),
        user.getNickname(),
        user.getIntroduction()
    );
  }
}
