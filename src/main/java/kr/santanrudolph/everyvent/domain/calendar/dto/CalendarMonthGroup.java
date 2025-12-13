package kr.santanrudolph.everyvent.domain.calendar.dto;

import java.util.List;

public record CalendarMonthGroup(
    int year,
    int month,
    List<CalendarSummaryResponse> calendars
) {
}
