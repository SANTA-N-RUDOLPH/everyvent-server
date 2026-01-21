package kr.santanrudolph.everyvent.domain.calendar.dto.reponse;

import kr.santanrudolph.everyvent.domain.calendar.Calendar;
import kr.santanrudolph.everyvent.domain.calendar.Official;
import kr.santanrudolph.everyvent.domain.calendar.enums.CalendarColor;
import kr.santanrudolph.everyvent.domain.calendar.enums.Category;
import kr.santanrudolph.everyvent.domain.calendar.enums.Visibility;

import java.time.LocalDate;

public record OfficialCalendarResponse(
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
    Long distributedCount
) {

  public static OfficialCalendarResponse from(Calendar calendar, Official official) {
    return new OfficialCalendarResponse(
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
        official.getDistributedCount()
    );
  }
}
