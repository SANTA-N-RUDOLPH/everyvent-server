package kr.santanrudolph.everyvent.domain.calendar.dto.reponse;

import kr.santanrudolph.everyvent.domain.calendar.Calendar;
import kr.santanrudolph.everyvent.domain.calendar.enums.CalendarColor;
import kr.santanrudolph.everyvent.domain.calendar.enums.CalendarType;
import kr.santanrudolph.everyvent.domain.calendar.enums.Category;
import kr.santanrudolph.everyvent.domain.calendar.enums.Visibility;

import java.time.LocalDate;

public record CalendarDetailResponse(
    Long id,
    Long userId,
    String title,
    String description,
    LocalDate startDate,
    LocalDate endDate,
    LocalDate previewStartDate,
    LocalDate previewEndDate,
    Visibility visibility,
    CalendarColor color,
    Category category,
    Long originalCalendarId,
    CalendarType type,
    boolean scrappable,
    Long scrapCount
) {

  public static CalendarDetailResponse from(Calendar calendar, boolean scrappable, Long scrapCount) {
    return new CalendarDetailResponse(
        calendar.getId(),
        calendar.getUser().getId(),
        calendar.getTitle(),
        calendar.getDescription(),
        calendar.getStartDate(),
        calendar.getEndDate(),
        calendar.getPreviewStartDay(),
        calendar.getPreviewEndDay(),
        calendar.getVisibility(),
        calendar.getColor(),
        calendar.getCategory(),
        calendar.getOriginalCalendarId(),
        calendar.getCalendarType(),
        scrappable,
        scrapCount
    );
  }

}
