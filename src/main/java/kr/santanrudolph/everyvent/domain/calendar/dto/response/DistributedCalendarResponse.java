package kr.santanrudolph.everyvent.domain.calendar.dto.response;

import kr.santanrudolph.everyvent.domain.calendar.entity.DistributedCalendar;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class DistributedCalendarResponse extends CalendarResponse {

  public static DistributedCalendarResponse from(DistributedCalendar calendar) {
    return fillCommonFields(DistributedCalendarResponse.builder(), calendar, false)
            .build();
  }
}