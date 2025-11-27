package kr.santanrudolph.everyvent.domain.calendar.dto.response;

import kr.santanrudolph.everyvent.domain.calendar.entity.OriginalCalendar;
import kr.santanrudolph.everyvent.domain.calendar.enums.Category;
import kr.santanrudolph.everyvent.domain.calendar.enums.Visibility;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class OriginalCalendarResponse extends CalendarResponse {

  private final LocalDate previewStartDate;
  private final LocalDate previewEndDate;

  @Builder
  private OriginalCalendarResponse(Long id,
                                   String title,
                                   String description,
                                   LocalDate startDate,
                                   LocalDate endDate,
                                   Visibility visibility,
                                   String color,
                                   Category category,
                                   Boolean isScrapable,
                                   LocalDate previewStartDate,
                                   LocalDate previewEndDate) {

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
