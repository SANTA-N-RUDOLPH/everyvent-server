package kr.santanrudolph.everyvent.domain.calendar.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
public class OriginalCalendarRequest extends CalendarRequest {

    private Instant previewStartDate;

    private Instant previewEndDate;

}
