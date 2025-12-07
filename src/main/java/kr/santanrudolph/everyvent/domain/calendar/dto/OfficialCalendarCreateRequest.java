package kr.santanrudolph.everyvent.domain.calendar.dto;

import kr.santanrudolph.everyvent.domain.calendar.enums.CalendarColor;
import kr.santanrudolph.everyvent.domain.calendar.enums.Category;

import java.time.LocalDate;

public record OfficialCalendarCreateRequest(
    String title,
    String description,
    LocalDate startDate,
    CalendarColor color,
    Category category
) {
}
