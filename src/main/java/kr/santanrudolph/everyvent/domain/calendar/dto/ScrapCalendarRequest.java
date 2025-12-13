package kr.santanrudolph.everyvent.domain.calendar.dto;

import jakarta.validation.constraints.NotNull;
import kr.santanrudolph.everyvent.domain.calendar.enums.CalendarColor;

public record ScrapCalendarRequest(
    @NotNull(message = "색상은 필수입니다.")
    CalendarColor color
) {
}
