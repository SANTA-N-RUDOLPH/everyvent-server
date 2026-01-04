package kr.santanrudolph.everyvent.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;


public record ProfileImageUpdateRequest(
    @NotBlank(message = "objectKey는 필수입니다.")
    String objectKey
) {
}
