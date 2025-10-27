package kr.santanrudolph.everyvent.domain.calendar;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.santanrudolph.everyvent.auth.AuthenticationUtil;
import kr.santanrudolph.everyvent.domain.calendar.dto.request.OriginalCalendarRequest;
import kr.santanrudolph.everyvent.domain.calendar.dto.response.CalendarResponse;
import kr.santanrudolph.everyvent.domain.calendar.dto.response.OriginalCalendarResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.util.List;

@Tag(name = "캘린더", description = "캘린더 관련 API")
@RestController
@RequestMapping("/api/calendars")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @Operation(summary = "캘린더 생성", description = "로그인한 사용자가 새로운 캘린더를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "캘린더 생성 성공"),
            @ApiResponse(responseCode = "400", description = "비즈니스 에러 (에러 코드로 구분)")
    })
    @PostMapping
    public ResponseEntity<OriginalCalendarResponse> createCalendar(
            @RequestHeader(value = "Time-Zone", defaultValue = "Asia/Seoul") String timeZone,
            @Valid @RequestBody OriginalCalendarRequest request
    ) {
        Long userId = AuthenticationUtil.getCurrentUserId();
        ZoneId userZone = ZoneId.of(timeZone);
        OriginalCalendarResponse response = calendarService.createCalendar(userId, request, userZone);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "공식 캘린더 생성 (관리자용)", description = "관리자가 새로운 공식 캘린더를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "공식 캘린더 생성 성공"),
            @ApiResponse(responseCode = "400", description = "비즈니스 에러 (에러 코드로 구분)")
    })
    @PostMapping("/official")
    public ResponseEntity<OriginalCalendarResponse> createOfficialCalendar(
            @RequestHeader(value = "Time-Zone", defaultValue = "Asia/Seoul") String timeZone,
            @Valid @RequestBody OriginalCalendarRequest request
    ) {
        Long adminId = AuthenticationUtil.getCurrentUserId();
        ZoneId userZone = ZoneId.of(timeZone);
        OriginalCalendarResponse response = calendarService.createOfficialCalendar(adminId, request, userZone);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "공식 캘린더 배포", description = "관리자가 생성한 공식 캘린더를 모든 일반 사용자에게 배포합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "공식 캘린더 배포 성공"),
            @ApiResponse(responseCode = "400", description = "비즈니스 에러 (에러 코드로 구분)")
    })
    @PostMapping("/{calendarId}/distribute")
    public ResponseEntity<Void> distributeOfficialCalendar(
            @PathVariable Long calendarId,
            @RequestHeader(value = "Time-Zone", defaultValue = "Asia/Seoul") String timeZone
    ) {
        Long userId = AuthenticationUtil.getCurrentUserId();
        ZoneId userZone = ZoneId.of(timeZone);
        calendarService.distributeOfficialCalendar(userId, calendarId, userZone);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "캘린더 상세 조회", description = "캘린더 ID로 특정 캘린더를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "비즈니스 에러 (에러 코드로 구분)")
    })
    @GetMapping("/{calendarId}")
    public ResponseEntity<CalendarResponse> getCalendar(
            @PathVariable Long calendarId,
            @RequestHeader(value = "Time-Zone", defaultValue = "Asia/Seoul") String timeZone
    ) {
        ZoneId userZone = ZoneId.of(timeZone);
        CalendarResponse response = calendarService.getCalendar(calendarId, userZone);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 캘린더 목록 조회", description = "현재 로그인한 사용자의 특정 년/월 캘린더 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "비즈니스 에러 (에러 코드로 구분)")
    })
    @GetMapping("/me")
    public ResponseEntity<List<CalendarResponse>> getMyCalendars(
            @RequestParam int year,
            @RequestParam int month,
            @RequestHeader(value = "Time-Zone", defaultValue = "Asia/Seoul") String timeZone
    ) {
        Long userId = AuthenticationUtil.getCurrentUserId();
        ZoneId userZone = ZoneId.of(timeZone);
        List<CalendarResponse> responses = calendarService.getMyCalendars(userId, year, month, userZone);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "다른 사용자 캘린더 조회", description = "다른 사용자의 캘린더 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "비즈니스 에러 (에러 코드로 구분)")
    })
    @GetMapping("/users/{targetId}")
    public ResponseEntity<List<CalendarResponse>> getCalendars(
            @PathVariable Long targetId,
            @RequestParam int year,
            @RequestParam int month,
            @RequestHeader(value = "Time-Zone", defaultValue = "Asia/Seoul") String timeZone
    ) {
        Long viewerId = AuthenticationUtil.getCurrentUserId();
        ZoneId userZone = ZoneId.of(timeZone);
        List<CalendarResponse> responses = calendarService.getCalendars(viewerId, targetId, year, month, userZone);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "캘린더 수정", description = "내가 만든 캘린더 또는 관리자가 만든 공식 캘린더를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "비즈니스 에러 (에러 코드로 구분)")
    })
    @PutMapping("/{calendarId}")
    public ResponseEntity<CalendarResponse> updateCalendar(
            @PathVariable Long calendarId,
            @RequestHeader(value = "Time-Zone", defaultValue = "Asia/Seoul") String timeZone,
            @Valid @RequestBody OriginalCalendarRequest request
    ) {
        Long userId = AuthenticationUtil.getCurrentUserId();
        ZoneId userZone = ZoneId.of(timeZone);
        CalendarResponse response = calendarService.updateCalendar(userId, calendarId, request, userZone);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "캘린더 삭제", description = "내가 만든 캘린더 또는 공식 캘린더를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "400", description = "비즈니스 에러 (에러 코드로 구분)")
    })
    @DeleteMapping("/{calendarId}")
    public ResponseEntity<Void> deleteCalendar(
            @PathVariable Long calendarId
    ) {
        Long userId = AuthenticationUtil.getCurrentUserId();
        calendarService.deleteCalendar(userId, calendarId);
        return ResponseEntity.ok().build();
    }
}