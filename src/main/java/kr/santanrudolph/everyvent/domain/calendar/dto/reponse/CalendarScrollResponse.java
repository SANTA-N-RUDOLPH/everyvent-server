package kr.santanrudolph.everyvent.domain.calendar.dto.reponse;

import java.util.List;

public record CalendarScrollResponse(
    List<CalendarMonthGroup> monthGroups,
    String nextCursor,
    boolean hasNext
) {
}
