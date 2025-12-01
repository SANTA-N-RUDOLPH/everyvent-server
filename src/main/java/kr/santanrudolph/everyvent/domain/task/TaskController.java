package kr.santanrudolph.everyvent.domain.task;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.santanrudolph.everyvent.auth.AuthenticationUtil;
import kr.santanrudolph.everyvent.domain.task.command.TaskUpdateCommand;
import kr.santanrudolph.everyvent.domain.task.dto.request.TaskCreateRequest;
import kr.santanrudolph.everyvent.domain.task.dto.response.TaskResponse;
import kr.santanrudolph.everyvent.domain.task.dto.response.TaskResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "태스크", description = "태스크 관련 API")
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

  private final TaskService taskService;

  @Operation(summary = "태스크 생성", description = "로그인한 사용자가 지정한 기간 동안 태스크를 생성합니다.")
  @ApiResponses({
          @ApiResponse(responseCode = "201", description = "태스크 생성 성공"),
          @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 잘못된 날짜 범위 또는 요청 값 오류"),
          @ApiResponse(responseCode = "403", description = "FORBIDDEN: 캘린더 접근 권한 없음"),
          @ApiResponse(responseCode = "404", description = "NOT_FOUND: 캘린더 또는 사용자 없음")
  })
  @PostMapping
  public ResponseEntity<TaskResponses> createTask(
          @Valid @RequestBody TaskCreateRequest request
  ) {
    Long userId = AuthenticationUtil.getCurrentUserId();
    TaskResponses response = taskService.createTask(userId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(summary = "공식 캘린더 태스크 생성", description = "관리자가 공식 캘린더에 지정한 기간 동안 태스크를 생성합니다.")
  @ApiResponses({
          @ApiResponse(responseCode = "201", description = "태스크 생성 성공"),
          @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 잘못된 날짜 범위 또는 요청 값 오류"),
          @ApiResponse(responseCode = "403", description = "FORBIDDEN: 공식 캘린더 접근 권한 없음"),
          @ApiResponse(responseCode = "404", description = "NOT_FOUND: 캘린더 또는 사용자 없음")
  })
  @PostMapping("/official")
  public ResponseEntity<TaskResponses> createOfficialTask(
          @Valid @RequestBody TaskCreateRequest request
  ) {
    Long userId = AuthenticationUtil.getCurrentUserId();
    TaskResponses response = taskService.createOfficialTask(userId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(summary = "태스크 조회", description = "로그인한 사용자가 소유한 일반 태스크를 조회합니다.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "태스크 조회 성공"),
          @ApiResponse(responseCode = "403", description = "FORBIDDEN: 접근 권한 없음"),
          @ApiResponse(responseCode = "404", description = "NOT_FOUND: 태스크 또는 사용자를 찾을 수 없음")
  })
  @GetMapping("/{taskId}")
  public ResponseEntity<TaskResponse> getTask(@PathVariable Long taskId) {
    Long userId = AuthenticationUtil.getCurrentUserId();
    TaskResponse response = taskService.getTask(userId, taskId);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "공식 캘린더 태스크 조회", description = "관리자가 공식 캘린더 태스크를 조회합니다.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "태스크 조회 성공"),
          @ApiResponse(responseCode = "403", description = "FORBIDDEN: 접근 권한 없음"),
          @ApiResponse(responseCode = "404", description = "NOT_FOUND: 태스크 또는 사용자를 찾을 수 없음")
  })
  @GetMapping("/official/{taskId}")
  public ResponseEntity<TaskResponse> getOfficialTask(@PathVariable Long taskId) {
    Long userId = AuthenticationUtil.getCurrentUserId();
    TaskResponse response = taskService.getOfficialTask(userId, taskId);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "태스크 수정", description = "로그인한 사용자가 소유한 태스크 이름 또는 날짜를 수정합니다.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "태스크 수정 성공"),
          @ApiResponse(responseCode = "403", description = "FORBIDDEN: 접근 권한 없음"),
          @ApiResponse(responseCode = "404", description = "NOT_FOUND: 태스크 또는 사용자를 찾을 수 없음")
  })
  @PutMapping("/{taskId}")
  public ResponseEntity<TaskResponse> updateTaskDetails(
          @PathVariable Long taskId,
          @Valid @RequestBody TaskUpdateCommand command
  ) {
    Long userId = AuthenticationUtil.getCurrentUserId();
    TaskResponse response = taskService.updateTaskDetails(userId, taskId, command);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "공식 캘린더 태스크 수정", description = "관리자가 공식 캘린더 태스크 이름 또는 날짜를 수정합니다.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "태스크 수정 성공"),
          @ApiResponse(responseCode = "403", description = "FORBIDDEN: 접근 권한 없음"),
          @ApiResponse(responseCode = "404", description = "NOT_FOUND: 태스크 또는 사용자를 찾을 수 없음")
  })
  @PutMapping("/official/{taskId}")
  public ResponseEntity<TaskResponse> updateOfficialTaskDetails(
          @PathVariable Long taskId,
          @Valid @RequestBody TaskUpdateCommand command
  ) {
    Long userId = AuthenticationUtil.getCurrentUserId();
    TaskResponse response = taskService.updateOfficialTaskDetails(userId, taskId, command);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "태스크 완료 상태 변경", description = "로그인한 사용자가 소유한 태스크의 완료 여부를 변경합니다.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "태스크의 완료 상태 수정 성공"),
          @ApiResponse(responseCode = "403", description = "FORBIDDEN: 접근 권한 없음"),
          @ApiResponse(responseCode = "404", description = "NOT_FOUND: 태스크 또는 사용자를 찾을 수 없음")
  })
  @PatchMapping("/completion/{taskId}")
  public ResponseEntity<TaskResponse> updateTaskCompletion(
          @PathVariable Long taskId,
          @RequestParam boolean isCompleted
  ) {
    Long userId = AuthenticationUtil.getCurrentUserId();
    TaskResponse response = taskService.updateTaskCompletion(userId, taskId, isCompleted);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "태스크 삭제", description = "로그인한 사용자가 소유한 태스크를 삭제합니다.")
  @ApiResponses({
          @ApiResponse(responseCode = "204", description = "태스크 삭제 성공"),
          @ApiResponse(responseCode = "403", description = "FORBIDDEN: 접근 권한 없음"),
          @ApiResponse(responseCode = "404", description = "NOT_FOUND: 태스크 또는 사용자를 찾을 수 없음")
  })
  @DeleteMapping("/{taskId}")
  public ResponseEntity<Void> deleteTask(
          @PathVariable Long taskId
  ) {
    Long userId = AuthenticationUtil.getCurrentUserId();
    taskService.deleteTask(userId, taskId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "공식 태스크 삭제", description = "관리자가 공식 캘린더 태스크를 삭제합니다.")
  @ApiResponses({
          @ApiResponse(responseCode = "204", description = "태스크 삭제 성공"),
          @ApiResponse(responseCode = "403", description = "FORBIDDEN: 접근 권한 없음"),
          @ApiResponse(responseCode = "404", description = "NOT_FOUND: 태스크 또는 사용자를 찾을 수 없음")
  })
  @DeleteMapping("/official/{taskId}")
  public ResponseEntity<Void> deleteOfficialTask(
          @PathVariable Long taskId
  ) {
    Long userId = AuthenticationUtil.getCurrentUserId();
    taskService.deleteOfficialTask(userId, taskId);
    return ResponseEntity.noContent().build();
  }
}
