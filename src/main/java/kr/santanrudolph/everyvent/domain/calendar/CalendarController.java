package kr.santanrudolph.everyvent.domain.calendar;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.santanrudolph.everyvent.domain.calendar.dto.*;
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
@Tag(name = "캘린더", description = "캘린더 관련 API - 생성, 조회, 수정, 삭제, 스크랩 기능을 제공합니다.")
@RestController
@RequestMapping("/api/calendars")
@RequiredArgsConstructor
public class CalendarController {

  @Operation(summary = "캘린더 생성", description = "유저가 내 캘린더(PERSONAL) 생성")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "생성 성공"),
      @ApiResponse(responseCode = "400", description = "INVALID_INPUT"),
      @ApiResponse(responseCode = "409", description = "ALREADY_EXIST: 캘린더 3개 이상")
  })
  @PostMapping
  public ResponseEntity<CalendarResponse> createCalendar(
      @Valid @RequestBody CalendarCreateRequest request) {

    CalendarResponse response = new CalendarResponse(
        1L,
        1L,
        request.title(),
        request.description(),
        request.startDate(),
        request.startDate().plusDays(25),
        request.visibility(),
        request.color(),
        request.category(),
        null,
        CalendarType.PERSONAL,
        true   // scrappable (원본이고 PUBLIC이면 true)
    );

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  // todo: 내가 만든 모든 캘린더 조회로 바꾸기 (visibility.admin은 제외)
  @Operation(summary = "캘린더 목록 조회", description = "쿼리 파라미터에 따라 다양한 캘린더 목록을 조회합니다.\n" +
      "- owner=me: 나의 모든 캘린더 (내가 만든 캘린더, 스크랩한 캘린더, 배포받은 캘린더)\n" +
      "- owner=me&year=2024&month=12: 특정 년월의 내 캘린더 통합 조회")
  @GetMapping
  public ResponseEntity<List<CalendarResponse>> getCalendars(
      @RequestParam(required = false) String owner,
      @RequestParam(required = false) Integer year,
      @RequestParam(required = false) Integer month) {

    CalendarResponse calendar = new CalendarResponse(
        1L,
        1L,
        "내 캘린더",
        "설명",
        LocalDate.of(2024, 12, 1),
        LocalDate.of(2024, 12, 25),
        Visibility.PUBLIC,
        CalendarColor.BLUE,
        Category.HOBBY,
        null,  // 원본 캘린더
        CalendarType.PERSONAL,
        true   // PUBLIC이므로 스크랩 가능
    );

    CalendarResponse calendar2 = new CalendarResponse(
        5L,
        1L,
        "내 캘린더2",
        "설명",
        LocalDate.of(2025, 12, 1),
        LocalDate.of(2025, 12, 25),
        Visibility.PRIVATE,
        CalendarColor.YELLOW,
        Category.CHALLENGE,
        null,  // 원본 캘린더
        CalendarType.PERSONAL,
        false  // PRIVATE이므로 스크랩 불가
    );

    CalendarResponse calendar6 = new CalendarResponse(
        6L,
        1L,
        "스크랩한 캘린더",
        "설명",
        LocalDate.of(2025, 12, 1),
        LocalDate.of(2025, 12, 25),
        Visibility.PRIVATE,
        CalendarColor.YELLOW,
        Category.CHALLENGE,
        10L,   // 원본 ID 존재
        CalendarType.SCRAPED,
        false  // 스크랩된 캘린더는 스크랩 불가
    );

    CalendarResponse calendar7 = new CalendarResponse(
        7L,
        1L,
        "배포받은 공식 캘린더",
        "설명",
        LocalDate.of(2025, 12, 1),
        LocalDate.of(2025, 12, 25),
        Visibility.PUBLIC,
        CalendarColor.MINT,
        Category.CHALLENGE,
        100L,  // 공식 캘린더 원본 ID
        CalendarType.OFFICIAL,
        false  // 공식 캘린더는 스크랩 불가
    );

    CalendarResponse calendar3 = new CalendarResponse(
        3L,
        1L,
        "내 캘린더3",
        "설명",
        LocalDate.of(2024, 11, 1),
        LocalDate.of(2024, 11, 25),
        Visibility.PRIVATE,
        CalendarColor.YELLOW,
        Category.CHALLENGE,
        null,
        CalendarType.PERSONAL,
        false
    );

    CalendarResponse calendar4 = new CalendarResponse(
        4L,
        1L,
        "내 캘린더4",
        "설명",
        LocalDate.of(2024, 10, 1),
        LocalDate.of(2024, 10, 25),
        Visibility.PRIVATE,
        CalendarColor.YELLOW,
        Category.CHALLENGE,
        null,
        CalendarType.PERSONAL,
        false
    );

    return ResponseEntity.ok(List.of(calendar, calendar2, calendar3, calendar4, calendar7, calendar6, calendar));
  }

  @Operation(summary = "캘린더 개별 조회", description = "공개범위에 따라 접근 제어. 내 캘린더 또는 권한 있는 캘린더만 조회 가능")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 접근 권한 없음"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND")
  })
  @GetMapping("/{calendarId}")
  public ResponseEntity<CalendarResponse> getCalendar(@PathVariable Long calendarId) {

    CalendarResponse response = new CalendarResponse(
        calendarId,
        2L,    // 다른 유저의 캘린더
        "캘린더 제목",
        "캘린더 설명",
        LocalDate.of(2024, 12, 1),
        LocalDate.of(2024, 12, 25),
        Visibility.PUBLIC,
        CalendarColor.MINT,
        Category.HOBBY,
        null,  // 원본 캘린더
        CalendarType.PERSONAL,
        true   // 이미 스크랩한 캘린더 판단 로직 필요...
    );

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "이번달 인기 캘린더 목록 조회(비회원도 접근 가능)", description = "이번달 인기 캘린더 목록을 조회합니다. 전체공개 캘린더만 조회됩니다. 아마두 10개 " +
      "아님 30개 반환으로 고정. 갯수는 프론트에게 맡김..")
  @GetMapping("/popular")
  public ResponseEntity<List<CalendarResponse>> getPopularCalendars() {

    CalendarResponse publicCalendar = new CalendarResponse(
        3L,
        3L,
        "공개 캘린더",
        "누구나 볼 수 있음",
        LocalDate.of(2024, 12, 1),
        LocalDate.of(2024, 12, 25),
        Visibility.PUBLIC,
        CalendarColor.PEACH,
        Category.CHALLENGE,
        null,  // 원본 캘린더
        CalendarType.PERSONAL,
        false // 비회원은 스크랩 버튼 비활성화
    );

    return ResponseEntity.ok(List.of(publicCalendar));
  }

  @Operation(summary = "캘린더 수정", description = "제목, 설명, 공개범위, 색상, 카테고리를 수정할 수 있습니다. 수정이 필요한 필드만 포함해서 보냅니다. (모든 필드를 추가하지 않아도 됨)")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "수정 성공"),
      @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 스크랩/배포본 수정 제한"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 소유자 아님"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND")
  })
  @PatchMapping("/{calendarId}")
  public ResponseEntity<CalendarResponse> updateCalendar(
      @PathVariable Long calendarId,
      @Valid @RequestBody CalendarUpdateRequest request) {

    CalendarResponse response = new CalendarResponse(
        calendarId,
        1L,
        request.getTitle() != null ? request.getTitle() : "기존 제목",
        request.getDescription() != null ? request.getDescription() : "기존 설명",
        LocalDate.of(2024, 12, 1),
        LocalDate.of(2024, 12, 25),
        request.getVisibility() != null ? request.getVisibility() : Visibility.PUBLIC,
        request.getColor() != null ? request.getColor() : CalendarColor.BLUE,
        request.getCategory() != null ? request.getCategory() : Category.CHALLENGE,
        null,  // 원본 캘린더
        CalendarType.PERSONAL,
        true
    );


    return ResponseEntity.ok(response);
  }


  @Operation(summary = "캘린더 삭제", description = "배포받은 공식 캘린더도 삭제 가능합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "삭제 성공"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 소유자 아님"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND")
  })
  @DeleteMapping("/{calendarId}")
  public ResponseEntity<Void> deleteCalendar(@PathVariable Long calendarId) {
    return ResponseEntity.noContent().build();
  }


  @Operation(summary = "캘린더 스크랩", description = "다른 사용자의 공개 캘린더를 내 컬렉션에 저장합니다. userId는 현재 유저입니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "스크랩 성공"),
      @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 자기 캘린더 스크랩 시도"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 스크랩 권한 없음"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND"),
      @ApiResponse(responseCode = "409", description = "ALREADY_EXIST: 이미 스크랩함")
  })
  @PostMapping("/{calendarId}/scrap")
  public ResponseEntity<CalendarResponse> scrapCalendar(
      @PathVariable Long calendarId) {

    CalendarResponse response = new CalendarResponse(
        50L,
        1L,          // 현재 유저 ID
        "다른 유저 캘린더",
        "스크랩한 캘린더 설명",
        LocalDate.of(2024, 12, 1),
        LocalDate.of(2024, 12, 25),
        Visibility.PRIVATE,  // 스크랩한 캘린더는 항상 PRIVATE
        CalendarColor.LAVENDER,
        Category.HOBBY,
        calendarId,  // 원본 캘린더 ID
        CalendarType.SCRAPED,
        false        // 스크랩된 캘린더는 재스크랩 불가
    );

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(summary = "스크랩한 캘린더 색상 변경", description = "스크랩한 캘린더의 색상만 변경 가능")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "색상 변경 성공"),
      @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 스크랩한 캘린더가 아님"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 소유자 아님"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND")
  })
  @PatchMapping("/{calendarId}/scrap")
  public ResponseEntity<CalendarResponse> updateScrapColor(
      @PathVariable Long calendarId,
      @Valid @RequestBody CalendarColor color) {

    CalendarResponse response = new CalendarResponse(
        calendarId,
        1L,
        "스크랩한 캘린더",
        "설명",
        LocalDate.of(2024, 12, 1),
        LocalDate.of(2024, 12, 25),
        Visibility.PRIVATE,
        color,  // 변경된 색상
        Category.HOBBY,
        10L,  // 원본 ID
        CalendarType.SCRAPED,
        false
    );

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "스크랩 취소", description = "스크랩한 캘린더를 삭제")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "스크랩 취소 성공"),
      @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 스크랩한 캘린더가 아님"),
      @ApiResponse(responseCode = "403", description = "FORBIDDEN: 소유자 아님"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND")
  })
  @DeleteMapping("/{calendarId}/scrap")
  public ResponseEntity<Void> cancelScrap(@PathVariable Long calendarId) {
    return ResponseEntity.noContent().build();
  }

/*
// ==================== 월별 조회 API ====================

  @Operation(summary = "월별 통합 캘린더 조회", description = "특정 년월의 내 모든 캘린더를 통합해서 조회")
  @GetMapping("/monthly")
  public ResponseEntity<List<CalendarResponse>> getMonthlyCalendars(
      @RequestParam int year,
      @RequestParam int month) {

    CalendarResponse calendar1 = new CalendarResponse(
        1L,
        "12월 캘린더1",
        "설명",
        LocalDate.of(year, month, 1),
        LocalDate.of(year, month, 25),
        Visibility.PUBLIC,
        CalendarColor.BLUE,
        Category.HOBBY,
        null,
        1L,
        CalendarType.PERSONAL,
        true
    );

    CalendarResponse calendar2 = new CalendarResponse(
        2L,
        "12월 캘린더2",
        "설명",
        LocalDate.of(year, month, 1),
        LocalDate.of(year, month, 25),
        Visibility.PRIVATE,
        CalendarColor.YELLOW,
        Category.CHALLENGE,
        null,
        1L,
        CalendarType.PERSONAL,
        false
    );

    return ResponseEntity.ok(List.of(calendar1, calendar2));
  }*/
}

