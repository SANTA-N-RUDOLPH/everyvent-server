package kr.santanrudolph.everyvent.domain.calendar.service;

import kr.santanrudolph.everyvent.domain.calendar.Calendar;
import kr.santanrudolph.everyvent.domain.calendar.Scrap;
import kr.santanrudolph.everyvent.domain.calendar.dto.*;
import kr.santanrudolph.everyvent.domain.calendar.enums.CalendarType;
import kr.santanrudolph.everyvent.domain.calendar.repository.CalendarRepository;
import kr.santanrudolph.everyvent.domain.task.TaskService;
import kr.santanrudolph.everyvent.domain.task.dto.TaskResponse;
import kr.santanrudolph.everyvent.domain.user.User;
import kr.santanrudolph.everyvent.domain.user.UserService;
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
  private final TaskService taskService;
  private final UserService userService;
  private final ScrapService scrapService;
  private final CalendarPolicy calendarPolicy;


  @Transactional
  public CalendarDetailResponse createPersonalCalendar(CalendarCreateRequest request) {

    User user = userService.getCurrentUser();

    calendarPolicy.validateCanCreate(user, request.startDate());

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
    log.info("Calendar created - userId={}, calendarId={}, startDate={}",
        user.getId(), saved.getId(), saved.getStartDate());

    return CalendarDetailResponse.from(saved, NOT_SCRAPPABLE, INITIAL_SCRAP_COUNT);
  }

  public CalendarDetailResponse getCalendar(Long calendarId) {
    Calendar calendar = findCalendarByIdAndDeletedAtIsNull(calendarId);
    User currentUser = userService.getCurrentUser();

    calendarPolicy.validateCanView(currentUser, calendar);

    Long scrapCount = scrapService.getScrapCount(calendarId);
    boolean scrappable = calendarPolicy.canScrap(currentUser, calendar);
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

    calendarPolicy.validateCanUpdate(currentUser, calendar);

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
  public CalendarDetailResponse scrapCalendar(Long calendarId, ScrapCalendarRequest request) {
    User currentUser = userService.getCurrentUser();
    Calendar originalCalendar = findCalendarByIdAndDeletedAtIsNull(calendarId);

    calendarPolicy.validateCanScrap(currentUser, originalCalendar);

    Calendar scrappedCalendar = Calendar.createScrappedCalendar(
        currentUser,
        originalCalendar,
        request.color()
    );

    // Calendar 저장
    Calendar savedCalendar = calendarRepository.save(scrappedCalendar);
    // Scrap 저장
    Scrap scrap = scrapService.addScrap(currentUser, originalCalendar);
    // Task 저장
    List<TaskResponse> taskResponses = taskService.saveCopyTasks(originalCalendar, savedCalendar);
    log.info("Calendar scrapped - originalCalendarId={}, newCalendarId={}, userId={}, copiedTaskCount={}",
        originalCalendar.getId(), savedCalendar.getId(), currentUser.getId(), taskResponses.size());

    Long scrapCount = scrap.getScrapCount();
    return CalendarDetailResponse.from(savedCalendar, NOT_SCRAPPABLE, scrapCount);
  }

  @Transactional
  public void cancelScrap(Long originalCalendarId) {
    User currentUser = userService.getCurrentUser();
    Calendar scrappedCalendar = calendarRepository
        .findByOriginalCalendarIdAndUserId(originalCalendarId, currentUser.getId())
        .orElseThrow(() ->
            new EveryventException(ErrorCode.NOT_FOUND, "스크랩하지 않은 캘린더입니다.")
        );

    calendarPolicy.validateCanCancelScrap(currentUser, scrappedCalendar);

    // calendar + task 삭제
    hardDeleteCalendar(scrappedCalendar.getId());
    // scrap에서 유저 삭제
    scrapService.removeScrap(currentUser, scrappedCalendar.getOriginalCalendarId());
    log.info("Scrap cancelled - originalCalendarId={}, scrappedCalendarId={}, userId={}",
        originalCalendarId, scrappedCalendar.getId(), currentUser.getId());

  }

  @Transactional
  public CalendarDetailResponse updateScrapColor(Long calendarId, ScrapCalendarRequest request) {
    User currentUser = userService.getCurrentUser();
    Calendar calendar = calendarRepository.findById(calendarId)
        .orElseThrow(() -> new EveryventException(ErrorCode.NOT_FOUND, "캘린더를 찾을 수 없습니다."));

    calendarPolicy.validateCanUpdateScrapColor(currentUser, calendar);

    calendar.updateColor(request.color());

    Long scrapCount = scrapService.getScrapCount(calendar.getId());
    return CalendarDetailResponse.from(calendar, NOT_SCRAPPABLE, scrapCount);
  }

  @Transactional
  public void deleteCalendar(Long calendarId) {
    User currentUser = userService.getCurrentUser();
    Calendar calendar = findCalendarByIdAndDeletedAtIsNull(calendarId);

    if (calendar.getCalendarType().equals(CalendarType.SCRAPED)) {
      log.info("Scraped calendar deleted (hard) - calendarId={}, userId={}",
          calendarId, currentUser.getId());
      hardDeleteCalendar(calendarId);
      return;
    }

    calendarPolicy.validateCanSoftDelete(currentUser, calendar);
    taskService.deleteByCalendarId(calendarId);
    log.info("Calendar soft deleted - calendarId={}, userId={}, type={}",
        calendarId, currentUser.getId(), calendar.getCalendarType());
    calendar.softDelete();
  }

  @Transactional
  public void hardDeleteCalendar(Long calendarId) {
    User currentUser = userService.getCurrentUser();
    Calendar calendar = findCalendarByIdAndDeletedAtIsNull(calendarId);

    calendarPolicy.validateCanHardDelete(currentUser, calendar);

    log.warn("Calendar permanently deleted - calendarId={}, userId={}, type={}",
        calendarId, currentUser.getId(), calendar.getCalendarType());
    taskService.deleteByCalendarId(calendarId);
    calendarRepository.deleteById(calendarId);
  }

  public Calendar findCalendarByIdAndDeletedAtIsNull(Long calendarId) {
    return calendarRepository.findByIdAndDeletedAtIsNull(calendarId)
        .orElseThrow(() -> new EveryventException(ErrorCode.NOT_FOUND, "캘린더를 찾을 수 없습니다."));
  }

  public ScrapperScrollResponse getScrappers(Long calendarId, Long cursor, Integer size) {
    if (size <= 0) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "size는 1 이상이어야 합니다.");
    }

    Calendar calendar = findCalendarByIdAndDeletedAtIsNull(calendarId);
    User currentUser = userService.getCurrentUser();

    calendarPolicy.validateCanView(currentUser, calendar);

    return scrapService.getScrappers(calendarId, cursor, size);
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

}
