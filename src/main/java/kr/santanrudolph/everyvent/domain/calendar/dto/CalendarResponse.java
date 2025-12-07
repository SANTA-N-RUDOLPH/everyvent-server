package kr.santanrudolph.everyvent.domain.calendar.dto;

import kr.santanrudolph.everyvent.domain.calendar.enums.CalendarColor;
import kr.santanrudolph.everyvent.domain.calendar.enums.CalendarType;
import kr.santanrudolph.everyvent.domain.calendar.enums.Category;
import kr.santanrudolph.everyvent.domain.calendar.enums.Visibility;

import java.time.LocalDate;

public record CalendarResponse(
    Long id,
    Long userId,
    String title,
    String description,
    LocalDate startDate,
    LocalDate endDate,
    Visibility visibility,
    CalendarColor color,
    Category category,
    Long originalCalendarId,
    CalendarType type,
    boolean scrappable
) {
}
