package kr.santanrudolph.everyvent.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "토큰 응답")
@Getter
@Builder
public class TokenResponse {

  @Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
  private String accessToken;

  @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
  private String refreshToken;

  @Schema(description = "토큰 타입", example = "Bearer")
  private String tokenType;

  @Schema(description = "Access Token 만료 시간(초)", example = "3600")
  private Long expiresIn;
}
