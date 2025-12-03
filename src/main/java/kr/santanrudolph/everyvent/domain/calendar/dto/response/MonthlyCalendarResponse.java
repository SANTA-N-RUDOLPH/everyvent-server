package kr.santanrudolph.everyvent.domain.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.santanrudolph.everyvent.domain.calendar.entity.Calendar;
import kr.santanrudolph.everyvent.domain.calendar.entity.OriginalCalendar;
import kr.santanrudolph.everyvent.domain.calendar.enums.CalendarType;
import kr.santanrudolph.everyvent.domain.calendar.enums.Category;
import kr.santanrudolph.everyvent.domain.calendar.enums.Visibility;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;

@Getter
public class MonthlyCalendarResponse extends CalendarResponse {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private LocalDate previewStartDate;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private LocalDate previewEndDate;
  private Map<Integer, Integer> dayTaskCount;

  private MonthlyCalendarResponse(
      Long id,
      Long userId,
      CalendarType type,
      String title,
      String description,
      LocalDate startDate,
      LocalDate endDate,
      LocalDate previewStartDate,
      LocalDate previewEndDate,
      Visibility visibility,
      String color,
      Category category,
      Boolean isScrapable,
      Map<Integer, Integer> dayTaskCount
  ) {
    super(id, userId, type, title, description, startDate, endDate, visibility, color, category, isScrapable);
    this.previewStartDate = previewStartDate;
    this.previewEndDate = previewEndDate;
    this.dayTaskCount = dayTaskCount;
  }

  public static MonthlyCalendarResponse from(Calendar calendar,
                                             CalendarType type,
                                             Boolean isScrapable,
                                             Map<Integer, Integer> dayTaskCount) {
    LocalDate previewStartDate = null;
    LocalDate previewEndDate = null;

    if(calendar instanceof OriginalCalendar originalCalendar) {
      previewStartDate = originalCalendar.getPreviewStartDate();
      previewEndDate = originalCalendar.getPreviewEndDate();
    }
    return new MonthlyCalendarResponse(
        calendar.getId(),
        calendar.getUser().getId(),
        type,
        calendar.getTitle(),
        calendar.getDescription(),
        calendar.getStartDate(),
        calendar.getEndDate(),
        previewStartDate,
        previewEndDate,
        calendar.getVisibility(),
        calendar.getColor(),
        calendar.getCategory(),
        isScrapable,
        dayTaskCount
    );
  }
}
