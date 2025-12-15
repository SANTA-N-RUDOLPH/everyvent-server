package kr.santanrudolph.everyvent.domain.calendar.dto;

import java.util.List;

public record PopularCalendarsResponse(
    List<CalendarListResponse> calendars
) {
}
