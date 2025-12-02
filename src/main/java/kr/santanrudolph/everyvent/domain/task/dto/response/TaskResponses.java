package kr.santanrudolph.everyvent.domain.task.dto.response;

import kr.santanrudolph.everyvent.domain.task.Task;

import java.util.List;

public record TaskResponses(
    List<TaskResponse> tasks
) {

  public static TaskResponses from(List<Task> entities) {
    List<TaskResponse> responses = entities.stream()
        .map(TaskResponse::from)
        .toList();

    return new TaskResponses(responses);
  }
}
