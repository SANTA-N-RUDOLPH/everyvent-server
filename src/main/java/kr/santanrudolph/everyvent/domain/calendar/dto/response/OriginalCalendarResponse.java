package kr.santanrudolph.everyvent.domain.calendar.dto.response;

import kr.santanrudolph.everyvent.domain.calendar.entity.OriginalCalendar;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter
@SuperBuilder
public class OriginalCalendarResponse extends CalendarResponse {

    private Instant previewStartDate;
    private Instant previewEndDate;

    public static OriginalCalendarResponse from(OriginalCalendar calendar, boolean isScrapable) {
        return fillCommonFields(OriginalCalendarResponse.builder(), calendar, isScrapable)
                .previewStartDate(calendar.getPreviewStartDate() != null
                        ? calendar.getPreviewStartDate() : null)
                .previewEndDate(calendar.getPreviewEndDate() != null
                        ? calendar.getPreviewEndDate() : null)
                .build();
    }
}