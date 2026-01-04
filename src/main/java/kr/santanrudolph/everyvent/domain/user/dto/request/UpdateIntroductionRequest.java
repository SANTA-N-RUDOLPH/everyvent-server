package kr.santanrudolph.everyvent.domain.user.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateIntroductionRequest {

  @Size(max = 500, message = "소개글은 최대500자까지 입력 가능합니다.")
  private String introduction;
}

/*
public record UpdateIntroductionRequest(
    @Size(max = 500, message = "소개글은 최대 500자까지 입력 가능합니다.")
    String introduction
) {}

*/
