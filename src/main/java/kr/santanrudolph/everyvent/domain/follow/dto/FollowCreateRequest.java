package kr.santanrudolph.everyvent.domain.follow.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FollowCreateRequest {

  @NotNull(message = "팔로우 대상 ID는 필수입니다.")
  @Min(value = 1, message = "팔로우 대상 ID는 1 이상이어야 합니다.")
  private Long targetId;

}
