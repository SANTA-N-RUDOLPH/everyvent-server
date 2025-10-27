package kr.santanrudolph.everyvent.domain.calendar.dto.response;

import kr.santanrudolph.everyvent.domain.calendar.OriginalCalendar;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.ZoneId;

@Getter
@SuperBuilder
public class OriginalCalendarResponse extends CalendarResponse {

    private LocalDate previewStartDate;
    private LocalDate previewEndDate;
    private Boolean isOfficial;
    private Long officialId;

    public static OriginalCalendarResponse from(OriginalCalendar calendar, boolean isScrapable, ZoneId userZone) {
        return OriginalCalendarResponse.builder()
                .id(calendar.getId())
                .title(calendar.getTitle())
                .description(calendar.getDescription() != null ? calendar.getDescription() : "")
                .startDate(toLocalDate(calendar.getStartDate(), userZone))
                .endDate(toLocalDate(calendar.getEndDate(), userZone))
                .visibility(calendar.getVisibility().name())
                .color(calendar.getColor())
                .category(calendar.getCategory().name())
                .isScrapable(isScrapable)
                .previewStartDate(calendar.getPreviewStartDate() != null
                        ? toLocalDate(calendar.getPreviewStartDate(), userZone) : null)
                .previewEndDate(calendar.getPreviewEndDate() != null
                        ? toLocalDate(calendar.getPreviewEndDate(), userZone) : null)
                .isOfficial(calendar.isOfficial())
                .officialId(calendar.getOfficialId())
                .build();
    }
}
