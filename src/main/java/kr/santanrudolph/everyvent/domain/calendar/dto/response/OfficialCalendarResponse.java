package kr.santanrudolph.everyvent.domain.calendar.dto.response;

import kr.santanrudolph.everyvent.domain.calendar.entity.OfficialCalendar;
import kr.santanrudolph.everyvent.domain.calendar.entity.OriginalCalendar;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter
@SuperBuilder
public class OfficialCalendarResponse extends CalendarResponse {

  private Instant distributedAt;

  public static OfficialCalendarResponse from(OfficialCalendar officialCalendar) {
    OriginalCalendar calendar = officialCalendar.getOriginalCalendar();

    return fillCommonFields(
            OfficialCalendarResponse.builder(), calendar, false
    ).distributedAt(officialCalendar.getDistributedAt())
    .build();
  }
}