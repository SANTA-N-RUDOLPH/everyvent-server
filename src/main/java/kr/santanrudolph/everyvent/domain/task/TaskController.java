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

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Tag(name = "태스크", description = "캘린더 태스크 관련 API. (내부 동작 미구현)")
@RestController
@RequestMapping("/api/calendars/{calendarId}/tasks")
@RequiredArgsConstructor
public class TaskController {


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

    List<TaskResponse> responses = requests.stream()
        .map(request -> new TaskResponse(
            1L,  // 실제로는 생성된 ID
            calendarId,
            request.day(),
            false,  // isBeforeToday
            request.canPreview(),
            request.content(),
            false  // completed
        ))
        .toList();

    return ResponseEntity.status(HttpStatus.CREATED).body(responses);
  }

  @Operation(summary = "특정 캘린더의 모든 태스크 조회 (개발용 임시 API)", description = "테스트 편의를 위해 캘린더의 모든 태스크 목록을 조회합니다. 개발용이므로 " +
      "content도 포함합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 접근 권한 없음"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND")
  })
  @GetMapping
  public ResponseEntity<List<TaskResponse>> getTasks(
      @PathVariable Long calendarId) {

    TaskResponse task1 = new TaskResponse(
        1L,
        calendarId,
        1,
        false,  // isBeforeToday
        true,   // canPreview
        "태스크 내용 1",
        true    // completed
    );

    TaskResponse task2 = new TaskResponse(
        2L,
        calendarId,
        2,
        true,   // isBeforeToday
        false,  // canPreview
        "태스크 내용 2",
        true    // completed
    );

    return ResponseEntity.ok(List.of(task1, task2));
  }

/*
  @Operation(summary = "캘린더별 태스크 상세 조회", description = "특정 태스크 상세 조회. canPreview=false이고 unlockDate 전이면 잠겨있음")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 접근 권한 없음 또는 아직 잠김"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND")
  })
  @GetMapping("/{taskId}")
  public ResponseEntity<TaskResponse> getTask(
      @PathVariable Long calendarId,
      @PathVariable Long taskId) {

    TaskResponse response = new TaskResponse(
        taskId,
        calendarId,
        5,
        "태스크 상세 내용",
        true,
        false,
        false
    );

    return ResponseEntity.ok(response);
  }*/

  // 특정 날짜의 task를 전부 조회합니다.
  @Operation(summary = "특정 캘린더의 day별 태스크 목록 조회", description = "캘린더의 일별 태스크 목록을 조회합니다." +
      "예: 1번 캘린더의 5일의 태스크 목록 조회")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 접근 권한 없음 또는 아직 잠김"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND")
  })
  @GetMapping("/{day}")
  public ResponseEntity<List<TaskResponse>> getTasksByCalendarIdAndDay(
      @PathVariable Long calendarId,
      @PathVariable Integer day) {

    TaskResponse response1 = new TaskResponse(
        1L,
        calendarId,
        day,
        false,  // isBeforeToday
        true,   // canPreview
        "태스크 상세 내용 1",
        false   // completed
    );

    TaskResponse response2 = new TaskResponse(
        2L,
        calendarId,
        day,
        false,  // isBeforeToday
        true,   // canPreview
        "태스크 상세 내용 2",
        false   // completed
    );

    TaskResponse response3 = new TaskResponse(
        3L,
        calendarId,
        day,
        false,  // isBeforeToday
        false,  // canPreview
        "태스크 상세 내용 3",
        false   // completed
    );

    return ResponseEntity.ok(List.of(response1, response2, response3));
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

    TaskResponse response = new TaskResponse(
        taskId,
        calendarId,
        5,
        false,  // isBeforeToday
        request.canPreview() != null ? request.canPreview() : true,
        request.content() != null ? request.content() : "기존 내용",
        false   // completed
    );

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

    TaskResponse response = new TaskResponse(
        taskId,
        calendarId,
        5,
        false,  // isBeforeToday
        true,   // canPreview
        "태스크 내용",
        completed
    );

    return ResponseEntity.ok(response);
  }
}
