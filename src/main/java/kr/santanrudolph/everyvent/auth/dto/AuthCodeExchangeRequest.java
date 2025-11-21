package kr.santanrudolph.everyvent.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "인증 코드 토큰 교환 요청")
public record AuthCodeExchangeRequest(
    @Schema(description = "일회성 인증 코드", example = "abc123-def456-ghi789")
    @NotBlank(message = "인증 코드는 필수입니다.")
    String code
) {

    public String getCode() {
        return code;
    }
}
