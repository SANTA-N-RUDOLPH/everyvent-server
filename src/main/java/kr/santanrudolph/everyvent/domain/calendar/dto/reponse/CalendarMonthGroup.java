package kr.santanrudolph.everyvent.domain.calendar.dto.reponse;

import java.time.YearMonth;
import java.util.List;

public record CalendarMonthGroup(
    int year,
    int month,
    List<CalendarListResponse> calendars
) {
  public YearMonth toYearMonth() {
    return YearMonth.of(year, month);
  }
}
