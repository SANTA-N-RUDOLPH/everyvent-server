package kr.santanrudolph.everyvent.domain.calendar.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Getter
@NoArgsConstructor
public class OriginalCalendarRequest extends CalendarRequest {

    private LocalDate previewStartDate;

    private LocalDate previewEndDate;

    private Boolean isOfficial;

    public Instant getPreviewStartInstant(ZoneId zone) {
        return previewStartDate != null ? previewStartDate.atStartOfDay(zone).toInstant() : null;
    }

    public Instant getPreviewEndInstant(ZoneId zone) {
        return previewEndDate != null ? previewEndDate.atStartOfDay(zone).toInstant() : null;
    }
}
