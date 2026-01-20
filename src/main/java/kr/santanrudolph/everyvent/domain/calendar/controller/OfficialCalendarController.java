package kr.santanrudolph.everyvent.domain.calendar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.santanrudolph.everyvent.domain.calendar.dto.reponse.CalendarDetailResponse;
import kr.santanrudolph.everyvent.domain.calendar.dto.reponse.CalendarListResponse;
import kr.santanrudolph.everyvent.domain.calendar.dto.reponse.DistributeResponse;
import kr.santanrudolph.everyvent.domain.calendar.dto.request.CalendarUpdateRequest;
import kr.santanrudolph.everyvent.domain.calendar.dto.request.OfficialCalendarCreateRequest;
import kr.santanrudolph.everyvent.domain.calendar.enums.CalendarColor;
import kr.santanrudolph.everyvent.domain.calendar.enums.CalendarType;
import kr.santanrudolph.everyvent.domain.calendar.enums.Category;
import kr.santanrudolph.everyvent.domain.calendar.enums.Visibility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Tag(name = "공식 캘린더", description = "공식 캘린더 관련 API (관리자 전용)")
@RestController
@RequestMapping("/api/admin/official/calendars")
@RequiredArgsConstructor
public class OfficialCalendarController {
  // todo: 전부

  @Operation(summary = "공식 캘린더 생성", description = "관리자만 가능. Visibility = ADMIN 자동 설정됨")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "생성 성공"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 관리자 아님")
  })
  @PostMapping()
  public ResponseEntity<CalendarDetailResponse> createOfficialCalendar(
      @Valid @RequestBody OfficialCalendarCreateRequest request) {

    CalendarDetailResponse response = new CalendarDetailResponse(
        100L,
        1L,    // 관리자 ID
        request.title(),
        request.description(),
        request.startDate(),
        request.startDate().plusDays(24),
        request.previewStartDate(),
        request.previewEndDate(),
        Visibility.ADMIN,
        request.color(),
        request.category(),
        null,  // 공식 캘린더는 원본
        CalendarType.OFFICIAL,
        false,  // 공식 캘린더는 스크랩 불가 (배포로만 가능)
        0L
    );

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(summary = "모든 공식 캘린더 목록 조회", description = "모든 공식 캘린더를 조회합니다.")
  @GetMapping()
  public ResponseEntity<List<CalendarListResponse>> getOfficialCalendars() {

    CalendarListResponse official = new CalendarListResponse(
        100L,
        1L,
        "2024 크리스마스 어드벤트",
        "공식 크리스마스 캘린더",
        LocalDate.of(2024, 12, 1),
        LocalDate.of(2024, 12, 25),
        Visibility.ADMIN,
        CalendarColor.MINT,
        Category.CHALLENGE,
        null,
        CalendarType.OFFICIAL,
        0L
    );

    CalendarListResponse official2 = new CalendarListResponse(
        101L,
        1L,
        "2024 크리스마스 어드벤트2",
        "공식 크리스마스 캘린더2",
        LocalDate.of(2024, 12, 1),
        LocalDate.of(2024, 12, 25),
        Visibility.ADMIN,
        CalendarColor.MINT,
        Category.CHALLENGE,
        null,
        CalendarType.OFFICIAL,
        0L
    );

    CalendarListResponse official3 = new CalendarListResponse(
        103L,
        1L,
        "2024 크리스마스 어드벤트3",
        "공식 크리스마스 캘린더3",
        LocalDate.of(2024, 12, 1),
        LocalDate.of(2024, 12, 25),
        Visibility.ADMIN,
        CalendarColor.MINT,
        Category.CHALLENGE,
        null,
        CalendarType.OFFICIAL,
        0L
    );

    return ResponseEntity.ok(List.of(official, official2, official3));
  }


  @Operation(summary = "공식 캘린더 배포", description = "관리자가 배포받지 않은 모든 유저에게 배포")
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
  @PutMapping("/{calendarId}")
  public ResponseEntity<CalendarDetailResponse> updateOfficialCalendar(
      @PathVariable Long calendarId,
      @Valid @RequestBody CalendarUpdateRequest request) {

    CalendarDetailResponse response = new CalendarDetailResponse(
        calendarId,
        1L,
        request.title() != null ? request.title() : "기존 제목",
        request.description() != null ? request.description() : "기존 설명",
        LocalDate.of(2024, 12, 1),
        LocalDate.of(2024, 12, 25),
        LocalDate.of(2024, 12, 1),
        LocalDate.of(2024, 12, 25),
        Visibility.ADMIN,
        request.color() != null ? request.color() : CalendarColor.BLUE,
        request.category() != null ? request.category() : Category.CHALLENGE,
        null,  // 원본 캘린더
        CalendarType.OFFICIAL,
        true,
        0L
    );

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "공식 캘린더 삭제", description = "관리자가 공식 캘린더 삭제")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "삭제 성공"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 관리자 아님"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND")
  })
  @DeleteMapping("/{calendarId}")
  public ResponseEntity<Void> deleteOfficialCalendar(@PathVariable Long calendarId) {
    return ResponseEntity.noContent().build();
  }

}

