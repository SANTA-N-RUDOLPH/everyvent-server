package kr.santanrudolph.everyvent.domain.calendar.dto;

import kr.santanrudolph.everyvent.domain.calendar.enums.CalendarColor;
import kr.santanrudolph.everyvent.domain.calendar.enums.Category;
import kr.santanrudolph.everyvent.domain.calendar.enums.Visibility;

import java.time.LocalDate;

public record CalendarCreateRequest(
    String title,
    String description,
    LocalDate startDate,
    Visibility visibility,
    CalendarColor color,
    Category category
) {
}
