package kr.santanrudolph.everyvent.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "토큰 재발급 요청")
@Getter
@NoArgsConstructor
public class TokenRefreshRequest {

  @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
  @NotBlank(message = "refreshToken은 필수입니다")
  private String refreshToken;
}
