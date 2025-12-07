package kr.santanrudolph.everyvent.domain.task.dto;

import jakarta.validation.constraints.Size;

public record TaskUpdateRequest(
    @Size(max = 500, message = "태스크 내용은 최대 500자까지 입력 가능합니다.")
    String content,

    Boolean canPreview
) {
    // 모든 필드 optional (부분 수정 지원)
}
