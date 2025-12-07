package kr.santanrudolph.everyvent.domain.task.dto;

import kr.santanrudolph.everyvent.domain.task.Task;
import kr.santanrudolph.everyvent.global.util.TimeUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class TaskResponse {

  private final Long id;
  private final Long calendarId;
  private final int day;
  private final boolean isBeforeToday;
  private final boolean canPreview;
  private String content;
  private boolean completed;

  public TaskResponse toTaskResponse(Task task) {
    boolean isBeforeToday = TimeUtil.isAfter(TimeUtil.today(), task.getDay());

    return new TaskResponse(
        task.getId(),
        task.getCalendar().getId(),
        task.getDay().getDayOfMonth(),
        isBeforeToday,
        task.isCanPreview(),
        task.getContent(),
        task.getCompleted()
    );
  }
}
