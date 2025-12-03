package kr.santanrudolph.everyvent.domain.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.santanrudolph.everyvent.domain.calendar.entity.Calendar;
import kr.santanrudolph.everyvent.domain.calendar.entity.OriginalCalendar;
import kr.santanrudolph.everyvent.domain.calendar.enums.CalendarType;
import kr.santanrudolph.everyvent.domain.calendar.enums.Category;
import kr.santanrudolph.everyvent.domain.calendar.enums.Visibility;
import kr.santanrudolph.everyvent.domain.task.dto.response.TaskResponse;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class DailyTasksOfCalendarResponse extends CalendarResponse {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private LocalDate previewStartDate;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private LocalDate previewEndDate;
  private LocalDate date;
  private List<TaskResponse> tasks;

  private DailyTasksOfCalendarResponse(
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
      LocalDate date,
      List<TaskResponse> tasks
  ) {
    super(id, userId, type, title, description, startDate, endDate, visibility, color, category, isScrapable);
    this.previewStartDate = previewStartDate;
    this.previewEndDate = previewEndDate;
    this.date = date;
    this.tasks = tasks;
  }

  public static DailyTasksOfCalendarResponse from(Calendar calendar,
                                                  CalendarType type,
                                                  LocalDate date,
                                                  Boolean isScrapable,
                                                  List<TaskResponse> tasks) {
    LocalDate previewStartDate = null;
    LocalDate previewEndDate = null;

    if(calendar instanceof OriginalCalendar originalCalendar) {
      previewStartDate = originalCalendar.getPreviewStartDate();
      previewEndDate = originalCalendar.getPreviewEndDate();
    }

    return
        new DailyTasksOfCalendarResponse(
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
        date,
        tasks
    );
  }
}
