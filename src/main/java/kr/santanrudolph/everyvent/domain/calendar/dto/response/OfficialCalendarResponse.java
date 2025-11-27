package kr.santanrudolph.everyvent.domain.calendar.dto.response;

import kr.santanrudolph.everyvent.domain.calendar.entity.OfficialCalendar;
import kr.santanrudolph.everyvent.domain.calendar.entity.OriginalCalendar;
import kr.santanrudolph.everyvent.domain.calendar.enums.Category;
import kr.santanrudolph.everyvent.domain.calendar.enums.Visibility;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
public class OfficialCalendarResponse extends CalendarResponse {

  private final Instant distributedAt;

  @Builder
  private OfficialCalendarResponse(Long id,
                                   String title,
                                   String description,
                                   LocalDate startDate,
                                   LocalDate endDate,
                                   Visibility visibility,
                                   String color,
                                   Category category,
                                   Boolean isScrapable,
                                   Instant distributedAt) {
    super(id, title, description, startDate, endDate, visibility, color, category, isScrapable);
    this.distributedAt = distributedAt;
  }

  public static OfficialCalendarResponse from(OfficialCalendar calendar) {

    OriginalCalendar original = calendar.getOriginalCalendar();
    return OfficialCalendarResponse.builder()
        .id(original.getId())
        .title(original.getTitle())
        .description(original.getDescription())
        .startDate(original.getStartDate())
        .endDate(original.getEndDate())
        .visibility(original.getVisibility())
        .color(original.getColor())
        .category(original.getCategory())
        .isScrapable(false)
        .distributedAt(calendar.getDistributedAt())
        .build();
  }
}
