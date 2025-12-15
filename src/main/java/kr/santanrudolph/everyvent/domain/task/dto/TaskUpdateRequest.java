package kr.santanrudolph.everyvent.domain.task.dto;

import jakarta.validation.constraints.Size;

public record TaskUpdateRequest(
    @Size(max = 500, message = "태스크 내용은 최대 500자까지 입력 가능합니다.")
    String content
) {

}
