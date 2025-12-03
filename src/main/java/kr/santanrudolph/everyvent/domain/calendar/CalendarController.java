package kr.santanrudolph.everyvent.domain.calendar;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.santanrudolph.everyvent.auth.AuthenticationUtil;
import kr.santanrudolph.everyvent.domain.calendar.dto.request.OriginalCalendarRequest;
import kr.santanrudolph.everyvent.domain.calendar.dto.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "캘린더", description = "캘린더 관련 API")
@RestController
@RequestMapping("/api/calendars")
@RequiredArgsConstructor
public class CalendarController {

  private final CalendarService calendarService;

  @Operation(summary = "원본 캘린더 생성", description = "로그인한 사용자가 새로운 캘린더를 생성합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "캘린더 생성 성공"),
      @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 캘린더 생성 가능 기간 또는 개수 제한"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 사용자를 찾을 수 없음")
  })
  @PostMapping
  public ResponseEntity<OriginalCalendarResponse> createCalendar(
      @Valid @RequestBody OriginalCalendarRequest request
  ) {
    Long userId = AuthenticationUtil.getCurrentUserId();
    OriginalCalendarResponse response = calendarService.createCalendar(userId, request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "공식 캘린더 생성 (관리자용)", description = "관리자가 새로운 공식 캘린더를 생성합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "공식 캘린더 생성 성공"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 관리자만 생성 가능"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 사용자를 찾을 수 없음")

  })
  @PostMapping("/official")
  public ResponseEntity<OfficialCalendarResponse> createOfficialCalendar(
      @Valid @RequestBody OriginalCalendarRequest request
  ) {
    Long adminId = AuthenticationUtil.getCurrentUserId();
    OfficialCalendarResponse response = calendarService.createOfficialCalendar(adminId, request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "공식 캘린더 배포 (관리자용)", description = "관리자가 생성한 공식 캘린더를 모든 일반 사용자에게 배포합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "공식 캘린더 배포 성공"),
      @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 캘린더 배포 가능 기간 제한"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 관리자만 배포 가능/삭제된 캘린더 배포 불가"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 사용자/캘린더를 찾을 수 없음")
  })
  @PostMapping("/distribute/{calendarId}")
  public ResponseEntity<List<DistributedCalendarResponse>> distributeOfficialCalendar(
      @PathVariable Long calendarId
  ) {
    Long adminId = AuthenticationUtil.getCurrentUserId();
    List<DistributedCalendarResponse> response = calendarService.distributeOfficialCalendar(adminId, calendarId);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "캘린더 상세 조회", description = "캘린더 ID로 특정 캘린더를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 삭제된 캘린더는 조회 불가"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 캘린더를 조회할 권한이 없음"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 사용자/캘린더를 찾을 수 없음")
  })
  @GetMapping("/{calendarId}")
  public ResponseEntity<CalendarResponse> getCalendar(
      @PathVariable Long calendarId
  ) {
    Long userId = AuthenticationUtil.getCurrentUserId();
    CalendarResponse response = calendarService.getCalendar(calendarId, userId);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "내 캘린더 목록 조회", description = "현재 로그인한 사용자가 생성한 모든 캘린더 목록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 요청 형식 오류"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 사용자를 찾을 수 없음")
  })
  @GetMapping("/me")
  public ResponseEntity<List<CalendarResponse>> getMyCalendars() {
    Long userId = AuthenticationUtil.getCurrentUserId();
    List<CalendarResponse> responses = calendarService.getMyCalendars(userId);
    return ResponseEntity.ok(responses);
  }

  @Operation(summary = "다른 사용자 캘린더 조회", description = "다른 사용자의 캘린더 목록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 요청 형식 오류"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 사용자를 찾을 수 없음")
  })
  @GetMapping("/others/{targetId}")
  public ResponseEntity<List<CalendarResponse>> getCalendars(
      @PathVariable Long targetId,
      @RequestParam int year,
      @RequestParam int month
  ) {
    Long viewerId = AuthenticationUtil.getCurrentUserId();
    List<CalendarResponse> responses = calendarService.getCalendars(viewerId, targetId, year, month);
    return ResponseEntity.ok(responses);
  }

  @Operation(summary = "배포된 캘린더 목록 조회", description = "특정 사용자가 받은 배포 캘린더 목록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 요청 형식 오류"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 캘린더를 조회할 권한이 없음"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 사용자를 찾을 수 없음")
  })
  @GetMapping("/distributed/{targetId}")
  public ResponseEntity<List<CalendarResponse>> getDistributedCalendars(
      @PathVariable Long targetId,
      @RequestParam int year,
      @RequestParam int month
  ) {
    Long viewerId = AuthenticationUtil.getCurrentUserId();
    List<CalendarResponse> responses = calendarService.getDistributedCalendars(viewerId, targetId, year, month);
    return ResponseEntity.ok(responses);
  }

  @Operation(
      summary = "모든 캘린더 월간 통계 조회",
      description = """
       특정 사용자가 가진 모든 캘린더의 월간 태스크 통계 정보(태스크 개수 및 완료 여부)를 조회합니다.
       로그인하지 않아도 데이터를 확인할 수 있습니다.
       """
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 조회 권한 없음"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 사용자를 찾을 수 없음")
  })
  @GetMapping("/stats/monthly/user/{targetId}")
  public ResponseEntity<List<MonthlyCalendarStatsResponse>> getAllMonthlyStats(
      @PathVariable Long targetId,
      @RequestParam int year,
      @RequestParam int month
  ) {
    Long viewerId = AuthenticationUtil.getCurrentUserIdOrNull();
    List<MonthlyCalendarStatsResponse> responses = calendarService.getAllMonthlyStats(viewerId, targetId, year, month);
    return ResponseEntity.ok(responses);
  }

  @Operation(summary = "단일 캘린더 월간 통계 조회", description = "특정 캘린더의 월간 태스크 통계 정보(태스크 개수)를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 조회 권한 없음"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 사용자/캘린더를 찾을 수 없음")
  })
  @GetMapping("/stats/monthly/calendar/{calendarId}")
  public ResponseEntity<MonthlyCalendarResponse> getMonthlyStats(
      @PathVariable Long calendarId
  ) {
    Long viewerId = AuthenticationUtil.getCurrentUserId();
    MonthlyCalendarResponse response = calendarService.getMonthlyStats(viewerId, calendarId);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "모든 캘린더 특정 날짜 태스크 조회",
      description = """
       특정 사용자가 가진 모든 캘린더에서 지정한 날짜의 태스크를 조회합니다.
       로그인하지 않아도 데이터를 확인할 수 있습니다.
       """
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 조회 권한 없음"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 사용자를 찾을 수 없음")
  })
  @GetMapping("/tasks/daily/user/{targetId}")
  public ResponseEntity<List<DailyTasksOfCalendarResponse>> getDailyTasksOfAllCalendars(
      @PathVariable Long targetId,
      @RequestParam LocalDate date
  ) {
    Long viewerId = AuthenticationUtil.getCurrentUserIdOrNull();
    List<DailyTasksOfCalendarResponse> responses = calendarService.getDailyTasksOfAllCalendars(viewerId, targetId, date);
    return ResponseEntity.ok(responses);
  }

  @Operation(summary = "단일 캘린더 특정 날짜 태스크 조회", description = "특정 캘린더에서 지정한 날짜의 태스크를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 조회 권한 없음"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 사용자/캘린더를 찾을 수 없음")
  })
  @GetMapping("/tasks/daily/calendar/{calendarId}")
  public ResponseEntity<DailyTasksOfCalendarResponse> getDailyTasksOfCalendar(
      @PathVariable Long calendarId,
      @RequestParam LocalDate date
  ) {
    Long viewerId = AuthenticationUtil.getCurrentUserId();
    DailyTasksOfCalendarResponse response = calendarService.getDailyTasksOfCalendar(viewerId, calendarId, date);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "공식 캘린더 목록 조회 (관리자용)", description = "관리자가 생성한 모든 공식 캘린더 목록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 요청 형식 오류"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 관리자만 조회 가능"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 사용자를 찾을 수 없음")
  })
  @GetMapping("/official")
  public ResponseEntity<List<CalendarResponse>> getOfficialCalendars() {
    Long adminId = AuthenticationUtil.getCurrentUserId();
    List<CalendarResponse> responses = calendarService.getOfficialCalendars(adminId);
    return ResponseEntity.ok(responses);
  }

  @Operation(summary = "공식 캘린더 월간 통계 조회 (관리자용)",
      description = "관리자가 등록한 특정 공식 캘린더의 월간 태스크 통계 정보(태스크 개수)를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 관리자만 조회 가능"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 사용자를 찾을 수 없음")
  })
  @GetMapping("/official/stats/monthly/{calendarId}")
  public ResponseEntity<MonthlyCalendarResponse> getOfficialMonthlyStats(
      @PathVariable Long calendarId
  ) {
    Long adminId = AuthenticationUtil.getCurrentUserId();
    MonthlyCalendarResponse response = calendarService.getOfficialMonthlyStats(adminId, calendarId);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "공식 캘린더 특정 날짜 태스크 조회 (관리자용)",
      description = "관리자가 등록한 특정 공식 캘린더에서 지정한 날짜의 태스크를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 관리자만 조회 가능"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 사용자/캘린더를 찾을 수 없음")
  })
  @GetMapping("/official/tasks/daily/{calendarId}")
  public ResponseEntity<DailyTasksOfCalendarResponse> getOfficialDailyTasksOfCalendar(
      @PathVariable Long calendarId,
      @RequestParam LocalDate date
  ) {
    Long adminId = AuthenticationUtil.getCurrentUserId();
    DailyTasksOfCalendarResponse response = calendarService.getOfficialDailyTasksOfCalendar(adminId, calendarId, date);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "원본 캘린더 수정", description = "로그인한 사용자가 생성한 원본 캘린더를 수정합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "수정 성공"),
      @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 요청 형식 오류/원본 캘린더만 수정 가능"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 캘린더 수정 권한 없음/삭제된 캘린더 수정 불가"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 사용자/캘린더를 찾을 수 없음")
  })
  @PutMapping("/{calendarId}")
  public ResponseEntity<CalendarResponse> updateOriginalCalendar(
      @PathVariable Long calendarId,
      @Valid @RequestBody OriginalCalendarRequest request
  ) {
    Long userId = AuthenticationUtil.getCurrentUserId();
    CalendarResponse response = calendarService.updateOriginalCalendar(userId, calendarId, request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "공식 캘린더 수정 (관리자용)", description = "관리자가 공식 캘린더를 수정합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "수정 성공"),
      @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 요청 형식 오류"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 관리자만 수정 가능/삭제된 캘린더 수정 불가"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 사용자/캘린더를 찾을 수 없음")
  })
  @PutMapping("/official/{calendarId}")
  public ResponseEntity<CalendarResponse> updateOfficialCalendar(
      @PathVariable Long calendarId,
      @Valid @RequestBody OriginalCalendarRequest request
  ) {
    Long adminId = AuthenticationUtil.getCurrentUserId();
    CalendarResponse response = calendarService.updateOfficialCalendar(adminId, calendarId, request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "캘린더 삭제", description = "로그인한 사용자가 소유한 캘린더를 삭제합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "삭제 성공"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 캘린더 삭제 권한 없음/삭제된 캘린더 삭제 불가"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 사용자/캘린더를 찾을 수 없음")
  })
  @DeleteMapping("/{calendarId}")
  public ResponseEntity<Void> deleteOriginalCalendar(
      @PathVariable Long calendarId
  ) {
    Long userId = AuthenticationUtil.getCurrentUserId();
    calendarService.deleteOriginalCalendar(userId, calendarId);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "공식 캘린더 삭제 (관리자용)", description = "관리자가 공식 캘린더를 삭제합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "삭제 성공"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 관리자만 삭제 가능/삭제된 캘린더 삭제 불가"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 사용자/캘린더를 찾을 수 없음")
  })
  @DeleteMapping("/official/{calendarId}")
  public ResponseEntity<Void> deleteOfficialCalendar(
      @PathVariable Long calendarId
  ) {
    Long adminId = AuthenticationUtil.getCurrentUserId();
    calendarService.deleteOfficialCalendar(adminId, calendarId);
    return ResponseEntity.ok().build();
  }
}

