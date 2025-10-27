package kr.santanrudolph.everyvent.domain.calendar.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class OriginalCalendarRequest extends CalendarRequest {

    private LocalDate previewStartDate;

    private LocalDate previewEndDate;

    private Boolean isOfficial;
}
