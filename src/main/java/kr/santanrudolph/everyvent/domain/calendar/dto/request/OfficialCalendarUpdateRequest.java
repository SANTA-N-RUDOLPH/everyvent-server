package kr.santanrudolph.everyvent.domain.calendar.dto.request;

import kr.santanrudolph.everyvent.domain.calendar.enums.CalendarColor;
import kr.santanrudolph.everyvent.domain.calendar.enums.Category;

import java.time.LocalDate;

public record OfficialCalendarUpdateRequest(

    String title,
    String description,
    LocalDate startDate,
    LocalDate endDate,
    LocalDate previewStartDate,
    LocalDate previewEndDate,
    CalendarColor color,
    Category category
) {

  public CalendarUpdateRequest toCalendarUpdateRequest() {
    return new CalendarUpdateRequest(
        this.title,
        this.description,
        this.startDate,
        this.endDate,
        this.previewStartDate,
        this.previewEndDate,
        null,
        this.color,
        this.category
    );
  }
}
