package kr.santanrudolph.everyvent.domain.task.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TaskCreateRequest(
    @NotNull(message = "날짜는 필수입니다.")
    @Min(value = 1, message = "날짜는 1일부터 시작합니다.")
    @Max(value = 25, message = "날짜는 25일까지 가능합니다.")
    Integer day,

    @NotBlank(message = "태스크 내용은 필수입니다.")
    @Size(max = 500, message = "태스크 내용은 최대 500자까지 입력 가능합니다.")
    String content

) {
}
