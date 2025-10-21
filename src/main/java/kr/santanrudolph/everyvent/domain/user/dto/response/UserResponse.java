package kr.santanrudolph.everyvent.domain.user.dto.response;

import kr.santanrudolph.everyvent.domain.user.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

  private Long id;
  private String nickname;
  private String email;
  private String introduction;
  private String socialProvider;
  private String role;

  public static UserResponse from(User user) {
    return UserResponse.builder()
        .id(user.getId())
        .nickname(user.getNickname())
        .email(user.getEmail())
        .introduction(user.getIntroduction())
        .socialProvider(user.getSocialProvider().name())
        .role(user.getRole().name())
        .build();
  }
}
