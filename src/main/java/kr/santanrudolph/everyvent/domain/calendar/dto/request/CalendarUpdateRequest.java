package kr.santanrudolph.everyvent.domain.calendar.dto.request;

import jakarta.validation.constraints.Size;
import kr.santanrudolph.everyvent.domain.calendar.enums.CalendarColor;
import kr.santanrudolph.everyvent.domain.calendar.enums.Category;
import kr.santanrudolph.everyvent.domain.calendar.enums.Visibility;

import java.time.LocalDate;

public record CalendarUpdateRequest(
    @Size(min = 1, max = 100, message = "캘린더 제목은 1~100자 사이여야 합니다")
    String title,

    @Size(max = 500, message = "설명은 최대 500자까지 입력 가능합니다")
    String description,
    LocalDate startDate,
    LocalDate endDate,
    LocalDate previewStartDate,
    LocalDate previewEndDate,
    Visibility visibility,
    CalendarColor color,
    Category category
) {
}
