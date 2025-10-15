package kr.santanrudolph.everyvent.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateNicknameRequest {

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣_]+$", message = "닉네임은 한글, 영문, 숫자, 언더스코어만 사용 가능합니다.")
    private String nickname;
}
