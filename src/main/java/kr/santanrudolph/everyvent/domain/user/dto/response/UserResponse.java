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
  private String provider;
  private String role;

  public static UserResponse from(User user) {
    return UserResponse.builder()
        .id(user.getId())
        .nickname(user.getNickname())
        .email(user.getEmail())
        .introduction(user.getIntroduction())
        .provider(user.getProvider())
        .role(user.getRole().name())
        .build();
  }
}
