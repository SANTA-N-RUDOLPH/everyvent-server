package kr.santanrudolph.everyvent.domain.task.dto.response;

import kr.santanrudolph.everyvent.domain.task.Task;

public record TaskResponse(
        Long id,
        Long calendarId,
        String name,
        int day,
        boolean isCompleted
) {
  public static TaskResponse from(Task task) {
    return new TaskResponse(
            task.getId(),
            task.getCalendar().getId(),
            task.getName(),
            task.getDay(),
            task.isCompleted()
    );
  }
}
