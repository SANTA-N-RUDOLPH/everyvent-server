package kr.santanrudolph.everyvent.domain.task;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.santanrudolph.everyvent.domain.task.dto.TaskCreateRequest;
import kr.santanrudolph.everyvent.domain.task.dto.TaskResponse;
import kr.santanrudolph.everyvent.domain.task.dto.TaskUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "태스크", description = "캘린더 태스크 관련 API")
@RestController
@RequestMapping("/api/calendars/{calendarId}/tasks")
@RequiredArgsConstructor
public class TaskController {

  private final TaskService taskService;


  @Operation(summary = "태스크 생성", description = "캘린더에 여러 태스크를 한 번에 생성합니다. 각 태스크별로 미리보기 여부를 지정할 수 있습니다. 미리보기는 해당 캘린더가 시작되는 달의 1일부터 쭉 볼 수 있습니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "생성 성공"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 소유자 아님 또는 스크랩/배포 캘린더"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND")
  })
  @PostMapping
  public ResponseEntity<List<TaskResponse>> createTasks(
      @PathVariable Long calendarId,
      @Valid @RequestBody List<TaskCreateRequest> requests) {

    List<TaskResponse> responses = taskService.createTasks(calendarId, requests);
    return ResponseEntity.status(HttpStatus.CREATED).body(responses);
  }

  @Operation(summary = "특정 캘린더의 모든 태스크 조회 (개발용 임시 API)", description = "테스트 편의를 위해 캘린더의 모든 태스크 목록을 조회합니다. 개발용이므로 " +
      "content도 포함합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 접근 권한 없음"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND")
  })
  @GetMapping("/all")
  public ResponseEntity<List<TaskResponse>> getTasksAllInformation(
      @PathVariable Long calendarId) {

    List<TaskResponse> responses = taskService.getTasksAllInformation(calendarId);
    return ResponseEntity.ok(responses);
  }


  @Operation(summary = "특정 캘린더의 모든 태스크 조회",
      description = """
          특정 캘린더의 모든 태스크 목록을 조회합니다.
          아직 열리지 않은 날짜는 isLock = true로, content와 completed는 null로 보냅니다.
          존재하지 않는 day는 포함하지 않고 보냅니다.
          """)
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 접근 권한 없음"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND")
  })
  @GetMapping
  public ResponseEntity<List<TaskResponse>> getTasks(
      @PathVariable Long calendarId) {

    List<TaskResponse> responses = taskService.getTasks(calendarId);
    return ResponseEntity.ok(responses);
  }


  @Operation(summary = "태스크 수정", description = "태스크 내용 및 미리보기 설정 수정")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "수정 성공"),
      @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 스크랩/배포 캘린더 수정 불가"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 소유자 아님"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND")
  })
  @PatchMapping("/{taskId}")
  public ResponseEntity<TaskResponse> updateTask(
      @PathVariable Long calendarId,
      @PathVariable Long taskId,
      @Valid @RequestBody TaskUpdateRequest request) {

    TaskResponse response = taskService.updateTask(calendarId, taskId, request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "태스크 삭제", description = "태스크 삭제")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "삭제 성공"),
      @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 스크랩/배포 캘린더 삭제 불가"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 소유자 아님"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND")
  })
  @DeleteMapping("/{taskId}")
  public ResponseEntity<Void> deleteTask(
      @PathVariable Long calendarId,
      @PathVariable Long taskId) {

    taskService.deleteTask(calendarId, taskId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "태스크 완료 상태 변경", description = "태스크의 완료/미완료 상태를 변경합니다. completed 쿼리 파라미터로 true/false를 전달합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "처리 성공"),
      @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 미래 태스크 완료 불가"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 접근 권한 없음"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND")
  })
  @PatchMapping("/{taskId}/complete")
  public ResponseEntity<TaskResponse> updateTaskCompletion(
      @PathVariable Long calendarId,
      @PathVariable Long taskId,
      @RequestParam Boolean completed) {

    TaskResponse response = taskService.updateTaskCompletion(calendarId, taskId, completed);
    return ResponseEntity.ok(response);
  }
}
