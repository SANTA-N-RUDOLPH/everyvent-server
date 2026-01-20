package kr.santanrudolph.everyvent.domain.calendar.dto.reponse;

import kr.santanrudolph.everyvent.domain.calendar.Calendar;
import kr.santanrudolph.everyvent.domain.calendar.enums.CalendarColor;
import kr.santanrudolph.everyvent.domain.calendar.enums.CalendarType;
import kr.santanrudolph.everyvent.domain.calendar.enums.Category;
import kr.santanrudolph.everyvent.domain.calendar.enums.Visibility;

import java.time.LocalDate;

public record CalendarListResponse(
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
    Long scrapCount
) {

  public static CalendarListResponse fromCalendar(Calendar calendar, Long scrapCount) {
    return new CalendarListResponse(
        calendar.getId(),
        calendar.getUser().getId(),
        calendar.getTitle(),
        calendar.getDescription(),
        calendar.getStartDate(),
        calendar.getEndDate(),
        calendar.getVisibility(),
        calendar.getColor(),
        calendar.getCategory(),
        calendar.getOriginalCalendarId(),
        calendar.getCalendarType(),
        scrapCount
    );
  }
}
