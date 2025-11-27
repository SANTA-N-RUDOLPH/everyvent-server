package kr.santanrudolph.everyvent.domain.calendar.dto.response;

import kr.santanrudolph.everyvent.domain.calendar.entity.DistributedCalendar;
import kr.santanrudolph.everyvent.domain.calendar.enums.Category;
import kr.santanrudolph.everyvent.domain.calendar.enums.Visibility;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class DistributedCalendarResponse extends CalendarResponse {

  @Builder
  private DistributedCalendarResponse(Long id,
                                      String title,
                                      String description,
                                      LocalDate startDate,
                                      LocalDate endDate,
                                      Visibility visibility,
                                      String color,
                                      Category category,
                                      Boolean isScrapable) {
    super(id, title, description, startDate, endDate, visibility, color, category, isScrapable);
  }

  public static DistributedCalendarResponse from(DistributedCalendar calendar) {
    return DistributedCalendarResponse.builder()
            .id(calendar.getId())
            .title(calendar.getTitle())
            .description(calendar.getDescription())
            .startDate(calendar.getStartDate())
            .endDate(calendar.getEndDate())
            .visibility(calendar.getVisibility())
            .color(calendar.getColor())
            .category(calendar.getCategory())
            .isScrapable(false)
            .build();
  }
}
