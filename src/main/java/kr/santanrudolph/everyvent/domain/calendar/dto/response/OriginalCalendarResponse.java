package kr.santanrudolph.everyvent.domain.calendar.dto.response;

import kr.santanrudolph.everyvent.domain.calendar.entity.OriginalCalendar;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.ZoneId;

@Getter
@SuperBuilder
public class OriginalCalendarResponse extends CalendarResponse {

    private LocalDate previewStartDate;
    private LocalDate previewEndDate;

    public static OriginalCalendarResponse from(OriginalCalendar calendar, boolean isScrapable) {
        ZoneId zone = ZoneId.of("Asia/Seoul");

        return fillCommonFields(OriginalCalendarResponse.builder(), calendar, isScrapable)
                .previewStartDate(calendar.getPreviewStartDate() != null
                        ? calendar.getPreviewStartDate().atZone(zone).toLocalDate() : null)
                .previewEndDate(calendar.getPreviewEndDate() != null
                        ? calendar.getPreviewEndDate().atZone(zone).toLocalDate() : null)
                .build();
    }
}