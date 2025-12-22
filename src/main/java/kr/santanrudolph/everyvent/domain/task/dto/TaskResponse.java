package kr.santanrudolph.everyvent.domain.task.dto;


import kr.santanrudolph.everyvent.domain.task.Task;


public record TaskResponse(
    Long id,
    Long calendarId,
    int day,
    boolean isLocked,
    String content,
    Boolean completed // null 상태 표현을 위함
) {
  public static TaskResponse from(Task task, boolean isLocked) {
    return new TaskResponse(
        task.getId(),
        task.getCalendar().getId(),
        task.getDay().getDayOfMonth(),
        isLocked,
        isLocked ? null : task.getContent(),
        isLocked ? null : task.getCompleted()
    );
  }
}
