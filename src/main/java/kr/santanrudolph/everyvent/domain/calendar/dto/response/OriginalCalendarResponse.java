package kr.santanrudolph.everyvent.domain.calendar.dto.response;

import kr.santanrudolph.everyvent.domain.calendar.entity.OriginalCalendar;
import kr.santanrudolph.everyvent.domain.calendar.enums.Category;
import kr.santanrudolph.everyvent.domain.calendar.enums.Visibility;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
public class OriginalCalendarResponse extends CalendarResponse {

    private final Instant previewStartDate;
    private final Instant previewEndDate;

    @Builder
    private OriginalCalendarResponse(Long id,
                                     String title,
                                     String description,
                                     Instant startDate,
                                     Instant endDate,
                                     Visibility visibility,
                                     String color,
                                     Category category,
                                     Boolean isScrapable,
                                     Instant previewStartDate,
                                     Instant previewEndDate) {

        super(id, title, description, startDate, endDate, visibility, color, category, isScrapable);
        this.previewStartDate = previewStartDate;
        this.previewEndDate = previewEndDate;
    }

    public static OriginalCalendarResponse from(OriginalCalendar calendar, boolean isScrapable) {
        return OriginalCalendarResponse.builder()
                .id(calendar.getId())
                .title(calendar.getTitle())
                .description(calendar.getDescription())
                .startDate(calendar.getStartDate())
                .endDate(calendar.getEndDate())
                .visibility(calendar.getVisibility())
                .color(calendar.getColor())
                .category(calendar.getCategory())
                .isScrapable(isScrapable)
                .previewStartDate(calendar.getPreviewStartDate())
                .previewEndDate(calendar.getPreviewEndDate())
                .build();
    }
}