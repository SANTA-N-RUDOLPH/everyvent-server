package kr.santanrudolph.everyvent.domain.task.dto;


import kr.santanrudolph.everyvent.domain.task.Task;


public record TaskResponse(
    Long id,
    Long calendarId,
    int day,
    boolean isLock,
    String content,
    Boolean completed // null 상태 표현을 위함
) {
  public static TaskResponse from(Task task, boolean isLock) {
    return new TaskResponse(
        task.getId(),
        task.getCalendar().getId(),
        task.getDay().getDayOfMonth(),
        isLock,
        isLock ? null : task.getContent(),
        isLock ? null : task.getCompleted()
    );
  }
}
