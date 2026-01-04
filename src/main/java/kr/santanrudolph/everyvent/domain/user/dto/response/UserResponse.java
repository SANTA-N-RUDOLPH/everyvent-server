package kr.santanrudolph.everyvent.domain.user.dto.response;


import kr.santanrudolph.everyvent.domain.user.User;

public record UserResponse(

    Long id,
    String nickname,
    String email,
    String introduction,
    String profileImageKey,
    String socialProvider,
    String role,
    boolean isNicknameRequired
) {

  public static UserResponse from(User user) {

    return new UserResponse(
        user.getId(),
        user.getNickname(),
        user.getEmail(),
        user.getIntroduction(),
        user.getProfileImageKey(),
        user.getSocialProvider().name(),
        user.getRole().name(),
        user.isNicknameRequired()
    );
  }
}
