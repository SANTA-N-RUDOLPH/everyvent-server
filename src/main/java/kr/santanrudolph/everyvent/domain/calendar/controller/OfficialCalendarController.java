package kr.santanrudolph.everyvent.domain.calendar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.santanrudolph.everyvent.domain.calendar.dto.reponse.DistributeResponse;
import kr.santanrudolph.everyvent.domain.calendar.dto.reponse.OfficialCalendarResponse;
import kr.santanrudolph.everyvent.domain.calendar.dto.request.OfficialCalendarCreateRequest;
import kr.santanrudolph.everyvent.domain.calendar.dto.request.OfficialCalendarUpdateRequest;
import kr.santanrudolph.everyvent.domain.calendar.service.OfficialCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "공식 캘린더", description = "공식 캘린더 관련 API (관리자 전용)")
@RestController
@RequestMapping("/api/admin/official/calendars")
@RequiredArgsConstructor
public class OfficialCalendarController {

  private final OfficialCalendarService officialCalendarService;

  @Operation(summary = "공식 캘린더 생성", description = "관리자만 가능")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "생성 성공"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 관리자 아님")
  })
  @PostMapping
  public ResponseEntity<OfficialCalendarResponse> createOfficialCalendar(
      @Valid @RequestBody OfficialCalendarCreateRequest request) {
    OfficialCalendarResponse response = officialCalendarService.createOfficialCalendar(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(summary = "모든 공식 캘린더 목록 조회", description = "모든 공식 캘린더를 조회합니다.")
  @GetMapping
  public ResponseEntity<List<OfficialCalendarResponse>> getOfficialCalendars() {
    List<OfficialCalendarResponse> response = officialCalendarService.getOfficialCalendars();
    return ResponseEntity.ok(response);
  }


  @Operation(summary = "공식 캘린더 배포(미완성)", description = "관리자가 배포받지 않은 모든 유저에게 배포")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "배포 성공"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 관리자 아님"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 해당 공식 캘린더 없음")
  })
  @PostMapping("/{officialCalendarId}/distribute")
  public ResponseEntity<DistributeResponse> distributeOfficialCalendar(
      @PathVariable Long officialCalendarId) {

    DistributeResponse response = new DistributeResponse(
        officialCalendarId,
        1523  // 배포된 유저 수
    );

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "공식 캘린더 수정", description = "관리자가 공식 캘린더 수정")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "수정 성공"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 관리자 아님"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND")
  })
  @PatchMapping("/{calendarId}")
  public ResponseEntity<OfficialCalendarResponse> updateOfficialCalendar(
      @PathVariable Long calendarId,
      @Valid @RequestBody OfficialCalendarUpdateRequest request) {
    OfficialCalendarResponse response = officialCalendarService.updateOfficialCalendar(calendarId, request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "공식 캘린더 삭제", description = """
      관리자가 공식 캘린더를 삭제합니다. 공식 캘린더는 반드시 해당 API로만 삭제해야합니다. (일반 캘린더 삭제 API 호출 금지)
      """)
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "삭제 성공"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 관리자 아님"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND")
  })
  @DeleteMapping("/{calendarId}")
  public ResponseEntity<Void> deleteOfficialCalendar(@PathVariable Long calendarId) {
    officialCalendarService.deleteOfficialCalendar(calendarId);
    return ResponseEntity.noContent().build();
  }

}

