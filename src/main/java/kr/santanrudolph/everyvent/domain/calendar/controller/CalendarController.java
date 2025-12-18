package kr.santanrudolph.everyvent.domain.calendar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.santanrudolph.everyvent.domain.calendar.service.CalendarService;
import kr.santanrudolph.everyvent.domain.calendar.dto.*;
import kr.santanrudolph.everyvent.domain.calendar.enums.CalendarColor;
import kr.santanrudolph.everyvent.domain.calendar.enums.CalendarType;
import kr.santanrudolph.everyvent.domain.calendar.enums.Category;
import kr.santanrudolph.everyvent.domain.calendar.enums.Visibility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@Tag(name = "캘린더", description = "캘린더 관련 API - 생성, 조회, 수정, 삭제, 스크랩 기능을 제공합니다.")
@RestController
@RequestMapping("/api/calendars")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @Operation(summary = "캘린더 생성", description = "유저가 내 캘린더(PERSONAL) 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "INVALID_INPUT"),
            @ApiResponse(responseCode = "409", description = "ALREADY_EXIST: 캘린더 3개 이상")
    })
    @PostMapping
    public ResponseEntity<CalendarDetailResponse> createCalendar(
            @Valid @RequestBody CalendarCreateRequest request) {

        CalendarDetailResponse response = calendarService.createPersonalCalendar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "내 캘린더 목록 조회 (무한스크롤)", description = "커서 기반 무한스크롤로 내 캘린더 목록을 월별로 그루핑하여 조회합니다.\n" +
            "(내가 만든 캘린더, 스크랩한 캘린더, 배포받은 캘린더)\n" +
            "- cursor: 이전 응답값에서 받은 nextCursor (YYYY-MM 형식, 첫 요청시 생략)\n" +
            "- size: 페이지 크기 (달 갯수를 의미합니다. 현재 예시는 7월~12월까지이므로 size=6 입니다.)")
    @GetMapping()
    public ResponseEntity<CalendarScrollResponse> getMyCalendars(
            @RequestParam(required = false) YearMonth cursor,
            @RequestParam(required = false, defaultValue = "10") Integer size) {

        CalendarScrollResponse response = calendarService.getMyCalendars(cursor, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "캘린더 개별 조회", description = "공개범위에 따라 접근 제어. 내 캘린더 또는 권한 있는 캘린더만 조회 가능")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN: 접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "NOT_FOUND")
    })
    @GetMapping("/{calendarId}")
    public ResponseEntity<CalendarDetailResponse> getCalendar(@PathVariable Long calendarId) {

        CalendarDetailResponse response = calendarService.getCalendar(calendarId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "월별 통합 캘린더 조회",
            description = """
                      특정 년월의 내 모든 캘린더를 조회합니다. (최대 4개)
                      - 조회 결과가 없을 경우 빈 배열([])을 반환합니다.
                      - 본인의 캘린더만 조회 가능합니다.
                    """
    )
    @GetMapping("/monthly")
    public ResponseEntity<List<CalendarDetailResponse>> getMonthlyCalendars(
            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM")
            YearMonth yearMonth
    ) {
        List<CalendarDetailResponse> response = calendarService.getMonthlyCalendars(yearMonth);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내가 만든 캘린더 수정", description = "제목, 설명, 공개범위, 색상, 카테고리를 수정할 수 있습니다. 수정이 필요한 필드만 포함해서 보냅니다. (모든 " +
            "필드를" +
            " " +
            "추가하지 않아도 됨)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 스크랩/배포본 수정 제한"),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN: 소유자 아님"),
            @ApiResponse(responseCode = "404", description = "NOT_FOUND")
    })
    @PatchMapping("/{calendarId}")
    public ResponseEntity<CalendarDetailResponse> updateCalendar(
            @PathVariable Long calendarId,
            @Valid @RequestBody CalendarUpdateRequest request) {

        CalendarDetailResponse response = calendarService.updatePersonalCalendar(calendarId, request);
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
        calendarService.deleteCalendar(calendarId);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "캘린더 스크랩", description = "다른 사용자의 공개 캘린더를 내 컬렉션에 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "스크랩 성공"),
            @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 자기 캘린더 스크랩 시도"),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN: 스크랩 권한 없음"),
            @ApiResponse(responseCode = "404", description = "NOT_FOUND"),
            @ApiResponse(responseCode = "409", description = "ALREADY_EXIST: 이미 스크랩함")
    })
    @PostMapping("/{calendarId}/scrap")
    public ResponseEntity<CalendarDetailResponse> scrapCalendar(
            @PathVariable Long calendarId) {

        CalendarDetailResponse response = new CalendarDetailResponse(
                50L,
                1L,          // 현재 유저 ID
                "다른 유저 캘린더",
                "스크랩한 캘린더 설명",
                LocalDate.of(2024, 12, 1),
                LocalDate.of(2024, 12, 25),
                LocalDate.of(2024, 12, 5),
                LocalDate.of(2024, 12, 20),
                Visibility.PRIVATE,  // 스크랩한 캘린더는 항상 PRIVATE
                CalendarColor.LAVENDER,
                Category.HOBBY,
                calendarId,  // 원본 캘린더 ID
                CalendarType.SCRAPED,
                false,        // 스크랩된 캘린더는 재스크랩 불가
                1L
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
    public ResponseEntity<CalendarDetailResponse> updateScrapColor(
            @PathVariable Long calendarId,
            @Valid @RequestBody ScrapCalendarRequest request) {

        CalendarDetailResponse response = calendarService.updateScrapColor(calendarId, request);
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

    // todo: 특정 캘린더를 스크랩한 사람 목록 조회하기

    @Operation(summary = "이번달 인기 캘린더 목록 조회 (비회원 접근 가능)", description = "이번달 인기 캘린더 30개를 조회합니다.\n" +
            "전체공개 캘린더만 조회됩니다.")
    @GetMapping("/popular")
    public ResponseEntity<List<CalendarListResponse>> getPopularCalendars() {

        List<CalendarListResponse> response = List.of(
                new CalendarListResponse(
                        101L, 11L, "인기 캘린더 1", "설명",
                        LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 25),
                        Visibility.PUBLIC, CalendarColor.BLUE, Category.CHALLENGE,
                        null, CalendarType.PERSONAL, 150L
                ),
                new CalendarListResponse(
                        102L, 12L, "인기 캘린더 2", "설명",
                        LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 25),
                        Visibility.PUBLIC, CalendarColor.PEACH, Category.HOBBY,
                        null, CalendarType.PERSONAL, 120L
                ),
                new CalendarListResponse(
                        103L, 13L, "인기 캘린더 3", "설명",
                        LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 25),
                        Visibility.PUBLIC, CalendarColor.MINT, Category.CHALLENGE,
                        null, CalendarType.PERSONAL, 100L
                ),
                new CalendarListResponse(
                        104L, 14L, "인기 캘린더 4", "설명",
                        LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 25),
                        Visibility.PUBLIC, CalendarColor.LAVENDER, Category.HOBBY,
                        null, CalendarType.PERSONAL, 90L
                ),
                new CalendarListResponse(
                        105L, 15L, "인기 캘린더 5", "설명",
                        LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 25),
                        Visibility.PUBLIC, CalendarColor.YELLOW, Category.CHALLENGE,
                        null, CalendarType.PERSONAL, 85L
                ),
                new CalendarListResponse(
                        106L, 16L, "인기 캘린더 6", "설명",
                        LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 25),
                        Visibility.PUBLIC, CalendarColor.BLUE, Category.HOBBY,
                        null, CalendarType.PERSONAL, 75L
                ),
                new CalendarListResponse(
                        107L, 17L, "인기 캘린더 7", "설명",
                        LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 25),
                        Visibility.PUBLIC, CalendarColor.PEACH, Category.CHALLENGE,
                        null, CalendarType.PERSONAL, 70L
                ),
                new CalendarListResponse(
                        108L, 18L, "인기 캘린더 8", "설명",
                        LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 25),
                        Visibility.PUBLIC, CalendarColor.MINT, Category.HOBBY,
                        null, CalendarType.PERSONAL, 65L
                ),
                new CalendarListResponse(
                        109L, 19L, "인기 캘린더 9", "설명",
                        LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 25),
                        Visibility.PUBLIC, CalendarColor.LAVENDER, Category.CHALLENGE,
                        null, CalendarType.PERSONAL, 60L
                ),
                new CalendarListResponse(
                        110L, 20L, "인기 캘린더 10", "설명",
                        LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 25),
                        Visibility.PUBLIC, CalendarColor.YELLOW, Category.HOBBY,
                        null, CalendarType.PERSONAL, 55L
                )
        );

        return ResponseEntity.ok(response);
    }
}

