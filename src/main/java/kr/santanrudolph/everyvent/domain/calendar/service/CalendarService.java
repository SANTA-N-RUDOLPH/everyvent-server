package kr.santanrudolph.everyvent.domain.calendar.service;

import kr.santanrudolph.everyvent.domain.calendar.Calendar;
import kr.santanrudolph.everyvent.domain.calendar.CalendarConstants;
import kr.santanrudolph.everyvent.domain.calendar.dto.*;
import kr.santanrudolph.everyvent.domain.calendar.enums.CalendarType;
import kr.santanrudolph.everyvent.domain.calendar.enums.Visibility;
import kr.santanrudolph.everyvent.domain.calendar.repository.CalendarRepository;
import kr.santanrudolph.everyvent.domain.follow.FollowService;
import kr.santanrudolph.everyvent.domain.user.User;
import kr.santanrudolph.everyvent.domain.user.UserService;
import kr.santanrudolph.everyvent.domain.user.enums.Role;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarService {

    private static final boolean SCRAPPABLE = true;
    private static final boolean NOT_SCRAPPABLE = false;
    private static final long INITIAL_SCRAP_COUNT = 0L;

    private final CalendarRepository calendarRepository;
    private final UserService userService;
    private final ScrapService scrapService;
    private final FollowService followService;


    @Transactional
    public CalendarDetailResponse createPersonalCalendar(CalendarCreateRequest request) {

        User user = userService.getCurrentUser();

        try {
            validateCalendarLimit(user, request.startDate());
            validateFuture(request.startDate());
        } catch (EveryventException e) {
            throw new EveryventException(ErrorCode.INVALID_INPUT, "캘린더를 만들 수 없습니다. " + e.getMessage());
        }

        Calendar calendar = Calendar.createCalendarWithStartDay(
                user,
                request.title(),
                request.description(),
                request.startDate(),
                request.previewStartDate(),
                request.previewEndDate(),
                request.visibility(),
                request.color(),
                request.category(),
                CalendarType.PERSONAL
        );


        Calendar saved = calendarRepository.save(calendar);

        return CalendarDetailResponse.from(saved, NOT_SCRAPPABLE, INITIAL_SCRAP_COUNT);
    }

    public CalendarDetailResponse getCalendar(Long calendarId) {
        Calendar calendar = findCalendarByIdAndDeletedAtIsNull(calendarId);
        User currentUser = userService.getCurrentUser();

        validateCanViewCalendar(currentUser, calendar);

        Long scrapCount = scrapService.getScrapCount(calendarId);
        boolean scrappable = isScrappable(currentUser, calendar);
        return CalendarDetailResponse.from(calendar, scrappable, scrapCount);
    }

    public CalendarScrollResponse getMyCalendars(YearMonth cursor, Integer size) {
        User user = userService.getCurrentUser();

        if (size <= 0) {
            throw new EveryventException(ErrorCode.INVALID_INPUT, "size는 1 이상이어야 합니다.");
        }

        YearMonth startYearMonth;
        if (cursor == null) {
            startYearMonth = calendarRepository
                    .findFirstByUserAndDeletedAtIsNullAndStartDateBeforeOrderByStartDateDesc(
                            user,
                            LocalDate.now().plusYears(10)  // todo: 비즈니스 로직 상 10년 후까지 포함. 리팩토링 필요
                    )
                    .map(calendar -> YearMonth.from(calendar.getStartDate()))
                    .orElse(null);
        } else {
            startYearMonth = cursor;
        }

        if (startYearMonth == null) { // 최근 캘린더 존재하지 않음
            return new CalendarScrollResponse(null, null, false);
        }

        YearMonth endYearMonth = startYearMonth.minusMonths(size);
        LocalDate startDate = endYearMonth.atDay(1);
        LocalDate endDate = startYearMonth.atEndOfMonth();

        List<Calendar> calendars = calendarRepository.findByUserAndStartDateBetween(
                user,
                startDate,
                endDate
        );

        Map<Long, Long> scrapCountsMap = getScrapCountMapFromCalendars(calendars);

        List<CalendarMonthGroup> calendarMonthGroups = groupCalendarsByMonth(calendars, scrapCountsMap);

        boolean hasNext = calendarMonthGroups.size() > size;
        String nextCursor = null;

        if (hasNext) {
            CalendarMonthGroup lastGroup = calendarMonthGroups.remove(calendarMonthGroups.size() - 1);
            nextCursor = lastGroup.toYearMonth().toString();
        }

        return new CalendarScrollResponse(calendarMonthGroups, nextCursor, hasNext);
    }

    public List<CalendarDetailResponse> getMonthlyCalendars(YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        List<Calendar> calendars = calendarRepository.findByUserAndStartDateBetween(
                userService.getCurrentUser(),
                startDate,
                endDate
        );

        Map<Long, Long> scrapCountsMap = getScrapCountMapFromCalendars(calendars);

        return calendars.stream()
                .map(calendar -> CalendarDetailResponse.from(
                        calendar,
                        NOT_SCRAPPABLE,
                        scrapCountsMap.getOrDefault(calendar.getId(), 0L)))
                .toList();
    }

    @Transactional
    public CalendarDetailResponse updatePersonalCalendar(Long calendarId, CalendarUpdateRequest request) {
        User currentUser = userService.getCurrentUser();
        Calendar calendar = findCalendarByIdAndDeletedAtIsNull(calendarId);

        validateUpdateCalendar(currentUser, calendar);

        if (request.title() != null) {
            calendar.updateTitle(request.title());
        }
        if (request.description() != null) {
            calendar.updateDescription(request.description());
        }
        if (request.startDate() != null) {
            calendar.updateStartDate(request.startDate());
        }
        if (request.endDate() != null) {
            calendar.updateEndDate(request.endDate());
        }
        if (request.previewStartDate() != null) {
            calendar.updatePreviewStartDay(request.previewStartDate());
        }
        if (request.previewEndDate() != null) {
            calendar.updatePreviewEndDay(request.previewEndDate());
        }
        if (request.visibility() != null) {
            calendar.updateVisibility(request.visibility());
        }
        if (request.color() != null) {
            calendar.updateColor(request.color());
        }
        if (request.category() != null) {
            calendar.updateCategory(request.category());
        }

        Long scrapCount = scrapService.getScrapCount(calendar.getId());

        return CalendarDetailResponse.from(calendar, false, scrapCount);
    }

    @Transactional
    public CalendarDetailResponse updateScrapColor(Long calendarId, ScrapCalendarRequest request) {
        User currentUser = userService.getCurrentUser();
        Calendar calendar = calendarRepository.findById(calendarId)
                .orElseThrow(() -> new EveryventException(ErrorCode.NOT_FOUND, "캘린더를 찾을 수 없습니다."));

        validateUpdateScrapColor(currentUser, calendar);

        calendar.updateColor(request.color());

        Long scrapCount = scrapService.getScrapCount(calendar.getId());
        return CalendarDetailResponse.from(calendar, SCRAPPABLE, scrapCount);
    }


    @Transactional
    public void deleteCalendar(Long calendarId) {
        User currentUser = userService.getCurrentUser();

        Calendar calendar = findCalendarByIdAndDeletedAtIsNull(calendarId);

        validateCanDeleteCalendar(currentUser, calendar);

        calendar.softDelete();
    }

    private Map<Long, Long> getScrapCountMapFromCalendars(List<Calendar> calendars) {
        if (calendars == null || calendars.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> calendarIds = calendars.stream()
                .map(Calendar::getId)
                .toList();
        return scrapService.getScrapCountMap(calendarIds);
    }

    private List<CalendarMonthGroup> groupCalendarsByMonth(List<Calendar> calendars, Map<Long, Long> scrapCountMap) {
        Map<YearMonth, List<CalendarListResponse>> groupedByMonth =
                calendars.stream()
                        .collect(Collectors.groupingBy(
                                calendar -> YearMonth.from(calendar.getStartDate()),
                                LinkedHashMap::new,
                                Collectors.mapping(
                                        calendar
                                                -> CalendarListResponse.fromCalendar(
                                                calendar, scrapCountMap.getOrDefault(calendar.getId(), 0L)
                                        ),
                                        Collectors.toList()
                                )
                        ));

        return groupedByMonth.entrySet().stream()
                .map(entry -> new CalendarMonthGroup(
                        entry.getKey().getYear(),
                        entry.getKey().getMonthValue(),
                        entry.getValue()
                ))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private Calendar findCalendarByIdAndDeletedAtIsNull(Long calendarId) {
        return calendarRepository.findByIdAndDeletedAtIsNull(calendarId)
                .orElseThrow(() -> new EveryventException(ErrorCode.NOT_FOUND, "캘린더를 찾을 수 없습니다."));
    }

    boolean canViewCalendar(User user, Calendar calendar) {
        if (user.getRole() == Role.ADMIN) { // 관리자는 다 조회 가능.
            return true;
        }

        if (isOwner(user, calendar)) {
            return true;
        }

        Visibility visibility = calendar.getVisibility();
        if (visibility == Visibility.PUBLIC) {
            return true;
        }

        Long followerId = user.getId();
        Long targetId = calendar.getUser().getId();
        if (visibility == Visibility.FOLLOWER) {
            return followService.isFollowing(followerId, targetId);
        }

        if (visibility == Visibility.MUTUAL) {
            return followService.isMutualFollow(followerId, targetId);
        }

        return false;
    }

    boolean isScrappable(User user, Calendar calendar) {
        if (isOwner(user, calendar)) {
            return false;
        }

        if (!canViewCalendar(user, calendar)) {
            return false;
        }

        return !scrapService.isScrapped(user, calendar.getId());
    }

    boolean isOwner(User user, Calendar calendar) {
        return calendar.getUser().getId().equals(user.getId());
    }

    private void validateCanViewCalendar(User user, Calendar calendar) {
        if (canViewCalendar(user, calendar)) {
            return;
        }
        throw new EveryventException(ErrorCode.INVALID_INPUT, "해당 캘린더에 접근 권한이 없습니다.");
    }

    private void validateCalendarLimit(User user, LocalDate targetDate) {
        int year = targetDate.getYear();
        int month = targetDate.getMonthValue();

        long count = calendarRepository.countByUserAndYearMonthAndTypes(
                user, year, month, CalendarConstants.LIMITED_CALENDAR_TYPES
        );

        if (count >= CalendarConstants.MAX_CNT_PER_MONTH) {
            throw new EveryventException(
                    ErrorCode.INVALID_INPUT,
                    String.format("%d년 %d월에 생성 가능한 캘린더 수를 초과했습니다.", year, month)
            );
        }
    }

    private void validateUpdateCalendar(User user, Calendar calendar) {
        try {
            validateFuture(calendar.getStartDate());
            validateDeleted(calendar);
            validateOwner(user, calendar);
            validateOriginalCalendar(calendar);
        } catch (EveryventException e) {
            throw new EveryventException(ErrorCode.INVALID_INPUT, "캘린더를 수정할 수 없습니다. " + e.getMessage());
        }
    }

    private void validateUpdateScrapColor(User user, Calendar calendar) {
        try {
            validateFuture(calendar.getStartDate());
            validateDeleted(calendar);
            validateOwner(user, calendar);
            validateScrappedCalendar(calendar);
        } catch (EveryventException e) {
            throw new EveryventException(ErrorCode.INVALID_INPUT, "캘린더 색상을 수정할 수 없습니다. " + e.getMessage());
        }
    }

    private void validateCanDeleteCalendar(User user, Calendar calendar) {
        validateFuture(calendar.getStartDate());
        validateDeleted(calendar);
        validateOwner(user, calendar);
    }

    private void validateFuture(LocalDate startDate) {
        YearMonth calendarYearMonth = YearMonth.from(startDate);
        YearMonth currentYearMonth = YearMonth.now();

        if (calendarYearMonth.isAfter(currentYearMonth)) {
            return;
        }
        throw new EveryventException(ErrorCode.INVALID_INPUT, "미래의 캘린더가 아닙니다.");
    }

    private void validateDeleted(Calendar calendar) {
        if (calendar.isDeleted()) {
            throw new EveryventException(ErrorCode.NOT_FOUND, "삭제된 캘린더입니다.");
        }
    }

    private void validateOwner(User user, Calendar calendar) {
        if (!isOwner(user, calendar)) {
            throw new EveryventException(ErrorCode.FORBIDDEN, "본인 캘린더가 아닙니다.");
        }
    }

    private void validateOriginalCalendar(Calendar calendar) {
        if (!calendar.isOriginalCalendar()) {
            throw new EveryventException(ErrorCode.FORBIDDEN, "원본 캘린더가 아닙니다.");
        }
    }

    private void validateScrappedCalendar(Calendar calendar) {
        if (calendar.getCalendarType().equals(CalendarType.SCRAPED)) {
            throw new EveryventException(ErrorCode.INVALID_INPUT, "스크랩한 캘린더가 아닙니다.");
        }
    }

}
