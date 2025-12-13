package kr.santanrudolph.everyvent.domain.calendar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kr.santanrudolph.everyvent.domain.calendar.enums.CalendarColor;
import kr.santanrudolph.everyvent.domain.calendar.enums.Category;

import java.time.LocalDate;

public record OfficialCalendarCreateRequest(
    @NotBlank(message = "캘린더 제목은 필수입니다")
    @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다")
    String title,

    @Size(max = 500, message = "설명은 500자를 초과할 수 없습니다")
    String description,

    @NotNull(message = "시작일은 필수입니다")
    LocalDate startDate,

    LocalDate previewStartDate,
    LocalDate previewEndDate,

    @NotNull(message = "색상은 필수입니다")
    CalendarColor color,

    @NotNull(message = "카테고리는 필수입니다")
    Category category
) {
}
