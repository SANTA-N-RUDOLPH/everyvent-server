package kr.santanrudolph.everyvent.domain.calendar;

import kr.santanrudolph.everyvent.domain.calendar.dto.request.OriginalCalendarRequest;
import kr.santanrudolph.everyvent.domain.calendar.dto.response.*;
import kr.santanrudolph.everyvent.domain.calendar.entity.Calendar;
import kr.santanrudolph.everyvent.domain.calendar.entity.DistributedCalendar;
import kr.santanrudolph.everyvent.domain.calendar.entity.OfficialCalendar;
import kr.santanrudolph.everyvent.domain.calendar.entity.OriginalCalendar;
import kr.santanrudolph.everyvent.domain.calendar.enums.CalendarType;
import kr.santanrudolph.everyvent.domain.calendar.enums.Visibility;
import kr.santanrudolph.everyvent.domain.follow.FollowService;
import kr.santanrudolph.everyvent.domain.task.Task;
import kr.santanrudolph.everyvent.domain.task.TaskRepository;
import kr.santanrudolph.everyvent.domain.task.dto.response.TaskResponse;
import kr.santanrudolph.everyvent.domain.user.enums.Role;
import kr.santanrudolph.everyvent.domain.user.User;
import kr.santanrudolph.everyvent.domain.user.UserRepository;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarService {

  private final CalendarRepository calendarRepository;
  private final OfficialCalendarRepository officialCalendarRepository;
  private final UserRepository userRepository;
  private final FollowService followService;
  private final TaskRepository taskRepository;
  private final JdbcTemplate jdbcTemplate;

  @Transactional
  public OriginalCalendarResponse createCalendar(Long userId, OriginalCalendarRequest request) {

    User user = getUserOrThrow(userId);
    validateCreateCalendar(request);

    // 한 달에 생성할 수 있는 캘린더 개수 제한 (최대 3개)
    validateMonthlyCalendarLimit(userId, request);
    log.info("캘린더 기간 시작일: {}", request.getStartDate());
    log.info("미리보기 기간 범위: {} ~ {}", request.getPreviewStartDate(), request.getPreviewEndDate());

    OriginalCalendar calendar = buildCalendar(user, request);
    calendarRepository.save(calendar);

    return OriginalCalendarResponse.from(calendar, calendar.isScrapable());
  }

  @Transactional
  public OfficialCalendarResponse createOfficialCalendar(Long adminId, OriginalCalendarRequest request) {

    User admin = getUserOrThrow(adminId);
    validateAdminRole(admin);

    OriginalCalendar calendar = buildCalendar(admin, request);
    calendarRepository.save(calendar);

    OfficialCalendar officialCalendar = new OfficialCalendar(calendar, null);
    officialCalendarRepository.save(officialCalendar);

    return OfficialCalendarResponse.from(officialCalendar);
  }

  @Transactional
  public List<DistributedCalendarResponse> distributeOfficialCalendar(Long adminId, Long calendarId) {

    OfficialCalendar officialCalendar = getOfficialCalendarOrThrow(calendarId, adminId);
    validateDistribution(officialCalendar);

    OriginalCalendar originalCalendar = officialCalendar.getOriginalCalendar();

    // 탈퇴하지 않은 사용자 중, 아직 해당 공식 캘린더 복제본을 가지지 않은 대상에게 배포
    List<User> targetUsers = userRepository.findTargetUsersForDistribution(originalCalendar.getId());

    List<DistributedCalendar> copies = createCalendarCopies(targetUsers, originalCalendar);
    copyTasksBulk(originalCalendar.getId());

    officialCalendar.updateDistributedAt(Instant.now());

    return copies.stream()
        .map(DistributedCalendarResponse::from)
        .toList();
  }

  public CalendarResponse getCalendar(Long calendarId, Long userId) {

    User viewer = getUserOrThrow(userId);
    Calendar calendar = getActiveCalendarOrThrow(calendarId);

    if (!canView(calendar, viewer, calendar.getUser())) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "해당 캘린더를 조회할 권한이 없습니다.");
    }

    boolean isScrapable = calendar.isScrapable() && !isCalendarOwner(viewer, calendar);
    return toCalendarResponse(calendar, isScrapable);
  }

  public List<CalendarResponse> getMyCalendars(Long userId) {

    User user = getUserOrThrow(userId);

    // TODO: ScrapedCalendar 구현 시, ScrapedCalendar도 response에 추가
    List<Calendar> calendars = calendarRepository.findOriginalCalendarsByUserId(user.getId());

    return toCalendarResponseList(calendars, user, user);
  }

  public List<CalendarResponse> getCalendars(Long userId, Long targetId, int year, int month) {

    User viewer = getUserOrThrow(userId);
    User targetUser = getUserOrThrow(targetId);

    LocalDate date = LocalDate.of(year, month, 1);
    List<Calendar> calendars = calendarRepository.findOriginalCalendarsByUserIdInMonth(targetUser.getId(), date);

    return toCalendarResponseList(calendars, viewer, targetUser);
  }

  public List<CalendarResponse> getDistributedCalendars(Long userId, Long targetId, int year, int month) {

    User viewer = getUserOrThrow(userId);
    User targetUser = getUserOrThrow(targetId);

    LocalDate date = LocalDate.of(year, month, 1);
    List<DistributedCalendar> distributedCalendars = calendarRepository
        .findDistributedCalendarsByUserIdInMonth(targetUser.getId(), date);

    return toCalendarResponseList(new ArrayList<>(distributedCalendars), viewer, targetUser);
  }

  public List<MonthlyCalendarStatsResponse> getAllMonthlyStats(Long userId, Long targetId, int year, int month) {

    User viewer = userId != null ? getUserOrThrow(userId) : null;
    User targetUser = getUserOrThrow(targetId);

    LocalDate date = LocalDate.of(year, month, 1);
    List<Calendar> calendars = calendarRepository.findCalendarsByUserIdInMonth(targetId, date);

    return calendars.stream()
        .filter(calendar -> canView(calendar, viewer, targetUser))
        .map(calendar -> toMonthlyAllCalendarStatsResponse(calendar, viewer))
        .toList();
  }

  public MonthlyCalendarResponse getMonthlyStats(Long userId, Long calendarId) {

    User viewer = getUserOrThrow(userId);

    Calendar calendar = getActiveCalendarOrThrow(calendarId);

    if (!canView(calendar, viewer, calendar.getUser())) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "해당 캘린더를 조회할 권한이 없습니다.");
    }

    return toMonthlyCalendarStatsResponse(calendar, viewer, true);
  }

  public List<DailyTasksOfCalendarResponse> getDailyTasksOfAllCalendars(Long userId, Long targetId, LocalDate date) {

    User viewer = userId != null ? getUserOrThrow(userId) : null;
    User targetUser = getUserOrThrow(targetId);

    LocalDate startDate = LocalDate.of(date.getYear(), date.getMonth(), 1);

    List<Calendar> calendars = calendarRepository.findCalendarsByUserIdInMonth(targetId, startDate);

    return calendars.stream()
        .filter(calendar -> canView(calendar, viewer, targetUser))
        .map(calendar -> toDailyTasksOfCalendarResponse(calendar, date, viewer, true))
        .toList();
  }

  public DailyTasksOfCalendarResponse getDailyTasksOfCalendar(Long userId, Long calendarId, LocalDate date) {

    User viewer = getUserOrThrow(userId);

    Calendar calendar = getActiveCalendarOrThrow(calendarId);

    if (!canView(calendar, viewer, calendar.getUser())) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "해당 캘린더를 조회할 권한이 없습니다.");
    }

    return toDailyTasksOfCalendarResponse(calendar, date, viewer, true);
  }

  public List<CalendarResponse> getOfficialCalendars(Long adminId) {

    User admin = getUserOrThrow(adminId);
    validateAdminRole(admin);

    List<OfficialCalendar> officialEntities = officialCalendarRepository.findAllOfficialCalendars();
    List<Calendar> officialCalendars = officialEntities.stream()
        .map(OfficialCalendar::getOriginalCalendar)
        .collect(Collectors.toList());

    return toCalendarResponseList(officialCalendars, admin, admin);
  }

  public MonthlyCalendarResponse getOfficialMonthlyStats(Long adminId, Long calendarId) {

    User admin = getUserOrThrow(adminId);

    OfficialCalendar officialCalendar = getOfficialCalendarOrThrow(calendarId, adminId);
    Calendar calendar = officialCalendar.getOriginalCalendar();

    return toMonthlyCalendarStatsResponse(calendar, admin, false);
  }

  public DailyTasksOfCalendarResponse getOfficialDailyTasksOfCalendar(Long adminId, Long calendarId, LocalDate date) {

    User admin = getUserOrThrow(adminId);

    OfficialCalendar officialCalendar = getOfficialCalendarOrThrow(calendarId, adminId);
    Calendar calendar = officialCalendar.getOriginalCalendar();

    return toDailyTasksOfCalendarResponse(calendar, date, admin, false);
  }

  @Transactional
  public CalendarResponse updateOriginalCalendar(Long userId, Long calendarId, OriginalCalendarRequest request) {

    Calendar calendar = getActiveCalendarOrThrow(calendarId);
    if (!(calendar instanceof OriginalCalendar originalCalendar)) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "원본 캘린더만 수정할 수 있습니다.");
    }

    if (officialCalendarRepository.existsByOriginalCalendarId(calendarId)) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "공식 캘린더 원본은 일반 수정 API에서 수정할 수 없습니다.");
    }

    User user = getUserOrThrow(userId);
    if (!isCalendarOwner(user, calendar)) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "해당 캘린더 수정 권한이 없습니다.");
    }

    LocalDate previewStartDate = request.getPreviewStartDate();
    LocalDate previewEndDate = request.getPreviewEndDate();

    validateEditCalendar(originalCalendar);
    updateOriginalCalendar(originalCalendar, request, previewStartDate, previewEndDate);

    return toCalendarResponse(originalCalendar, originalCalendar.isScrapable());
  }

  @Transactional
  public CalendarResponse updateOfficialCalendar(Long adminId, Long calendarId, OriginalCalendarRequest request) {

    OriginalCalendar calendar = getOfficialCalendarOrThrow(calendarId, adminId).getOriginalCalendar();

    validateEditCalendar(calendar);
    updateOriginalCalendar(calendar, request, null, null);

    return toCalendarResponse(calendar, false);
  }

  // TODO: 추후 ScrapedCalendar 구현 시, updateScrapedCalendar 추가

  @Transactional
  public void deleteOriginalCalendar(Long userId, Long calendarId) {

    Calendar calendar = getActiveCalendarOrThrow(calendarId);
    User user = getUserOrThrow(userId);

    if (calendar instanceof OriginalCalendar originalCalendar &&
        officialCalendarRepository.existsByOriginalCalendarId(originalCalendar.getId())) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "공식 캘린더 원본은 일반 삭제 API에서 삭제할 수 없습니다.");
    }

    if (!isCalendarOwner(user, calendar) && user.getRole() != Role.ADMIN) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "해당 캘린더를 삭제할 권한이 없습니다.");
    }

    List<Task> tasks = taskRepository.findByCalendarIdAndDeletedAtIsNull(calendar.getId());

    calendar.softDelete();
    tasks.forEach(Task::softDelete);

    calendarRepository.save(calendar);
    taskRepository.saveAll(tasks);
  }

  @Transactional
  public void deleteOfficialCalendar(Long adminId, Long calendarId) {

    OfficialCalendar official = getOfficialCalendarOrThrow(calendarId, adminId);
    OriginalCalendar calendar = official.getOriginalCalendar();

    List<Task> tasks = taskRepository.findByCalendarIdAndDeletedAtIsNull(calendar.getId());

    calendar.softDelete();
    official.softDelete();
    tasks.forEach(Task::softDelete);

    calendarRepository.save(calendar);
    officialCalendarRepository.save(official);
    taskRepository.saveAll(tasks);
  }

  // ==================== Private Helper Methods ====================

  // 엔티티/객체 관련
  private User getUserOrThrow(Long userId) {
    return userRepository.findByIdAndDeletedAtIsNull(userId)
        .orElseThrow(() -> new EveryventException(ErrorCode.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."));
  }

  private Calendar getActiveCalendarOrThrow(Long calendarId) {
    Calendar calendar = calendarRepository.findById(calendarId)
        .orElseThrow(() -> new EveryventException(ErrorCode.NOT_FOUND, "해당 캘린더를 찾을 수 없습니다."));

    if (calendar.getDeletedAt() != null) {
      throw new EveryventException(ErrorCode.NOT_FOUND, "이미 삭제된 캘린더입니다.");
    }

    return calendar;
  }

  private OfficialCalendar getOfficialCalendarOrThrow(Long calendarId, Long adminId) {
    User admin = getUserOrThrow(adminId);
    validateAdminRole(admin);

    OfficialCalendar officialCalendar = officialCalendarRepository.findByOriginalCalendarId(calendarId)
        .orElseThrow(() ->
            new EveryventException(ErrorCode.NOT_FOUND, "해당 캘린더는 존재하지 않거나 공식 캘린더가 아닙니다."));

    if (officialCalendar.getDeletedAt() != null) {
      throw new EveryventException(ErrorCode.NOT_FOUND, "삭제된 원본 캘린더엔 더 이상 접근할 수 없습니다.");
    }

    return officialCalendar;
  }

  // 권한/검증 관련
  private void validateAdminRole(User user) {
    if (user.getRole() != Role.ADMIN) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "공식 캘린더에 접근할 권한이 없습니다.");
    }
  }

  private void validateCreateCalendar(OriginalCalendarRequest request) {
    YearMonth now = YearMonth.now();
    YearMonth calendarMonth = YearMonth.from(request.getStartDate());

    if (!calendarMonth.isAfter(now)) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "다음 달부터의 캘린더를 생성할 수 있습니다.");
    }
  }

  private void validateDistribution(OfficialCalendar officialCalendar) {
    LocalDate startDate = officialCalendar.getOriginalCalendar().getStartDate();
    YearMonth now = YearMonth.now();
    YearMonth calendarMonth = YearMonth.from(startDate);

    if (now.isAfter(calendarMonth)) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "공식 캘린더는 해당 월 이후에는 배포할 수 없습니다.");
    }
  }

  private void validateEditCalendar(Calendar calendar) {
    YearMonth now = YearMonth.now();
    YearMonth calendarMonth = YearMonth.from(calendar.getStartDate());

    if (!now.isBefore(calendarMonth)) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "캘린더 해당 월부터는 수정할 수 없습니다.");
    }
  }

  private void validateMonthlyCalendarLimit(Long userId, OriginalCalendarRequest request) {
    long userMonthlyCalendarCount = calendarRepository
        .countByUserIdInMonth(userId, request.getStartDate());

    if (userMonthlyCalendarCount >= 3) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "캘린더는 최대 3개까지 생성할 수 있습니다.");
    }
  }

  private void validateDateRange(LocalDate startDate, LocalDate endDate) {
    if (startDate.isAfter(endDate)) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "미리보기 기간의 시작일은 종료일보다 앞서야 합니다.");
    }
  }

  private void validateWithinCalendarPeriod(LocalDate startDate, LocalDate endDate, LocalDate start, LocalDate end) {
    if (start.isBefore(startDate) || end.isAfter(endDate)) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "미리보기 기간은 캘린더 기간 안에 포함되어야 합니다.");
    }

    if (end.getDayOfMonth() > 7) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "미리보기 종료일은 해당 월의 7일 이내여야 합니다.");
    }
  }

  private boolean isOwner(User viewer, User targetUser) {
    return viewer.getId().equals(targetUser.getId());
  }

  private boolean isCalendarOwner(User user, Calendar calendar) {
    if(user == null){
      return false;
    }
    return Objects.equals(calendar.getUser().getId(), user.getId());
  }

  private boolean canView(Calendar calendar, User viewer, User targetUser) {
    // 비회원: PUBLIC 캘린더만 허용
    if (viewer == null) {
      return calendar.getVisibility() == Visibility.PUBLIC;
    }

    if (isOwner(viewer, targetUser)) {
      return true;
    }

    return switch (calendar.getVisibility()) {
      case PUBLIC -> true;
      case MUTUAL -> followService.isMutualFollow(viewer.getId(), targetUser.getId());
      case FOLLOWER -> followService.isFollowing(viewer.getId(), targetUser.getId());
      case PRIVATE -> false;
    };
  }

  private boolean canViewDate(Calendar calendar, LocalDate date, User viewer) {

    LocalDate now = LocalDate.now();
    YearMonth calendarMonth = YearMonth.from(calendar.getStartDate());
    YearMonth nowMonth = YearMonth.from(now);

    boolean isOwner = isCalendarOwner(viewer, calendar);

    // 1) 현재 달 → 오늘 날짜까지만 공개
    if (calendarMonth.equals(nowMonth)) {
      return date.getDayOfMonth() <= now.getDayOfMonth();
    }

    // 2) 과거 달 → 전체 공개
    if (calendarMonth.isBefore(nowMonth)) {
      return true;
    }

    // 3) 미래 달
    // 3-1) 자신의 캘린더 → 전체 공개
    if (isOwner) {
      return true;
    }

    // 3-2) 다른 사용자의 캘린더 → preview 기간만 공개
    if (calendar instanceof OriginalCalendar originalCalendar) {
      LocalDate previewStart = originalCalendar.getPreviewStartDate();
      LocalDate previewEnd = originalCalendar.getPreviewEndDate();

      if (previewStart == null || previewEnd == null) {
        return false;
      }

      return !date.isBefore(previewStart) && !date.isAfter(previewEnd);
    }

    return false;
  }

  private CalendarType determineCalendarType(Calendar calendar) {
    if (calendar instanceof OriginalCalendar) {
      OfficialCalendar official = officialCalendarRepository.findByOriginalCalendarId(calendar.getId())
          .orElse(null);
      return (official != null) ? CalendarType.OFFICIAL : CalendarType.ORIGINAL;
    } else if (calendar instanceof DistributedCalendar) {
      return CalendarType.DISTRIBUTED;
    } else {
      return CalendarType.SCRAPED;
    }
  }

  private List<Task> filterMonthlyTasks(Calendar calendar, List<Task> tasks, User viewer) {
    return tasks.stream()
        .filter(task -> {
          LocalDate taskDate = calendar.getStartDate().withDayOfMonth(task.getDay());
          return canViewDate(calendar, taskDate, viewer);
        })
        .toList();
  }

  // 생성/업데이트 관련
  private OriginalCalendar buildCalendar(User user, OriginalCalendarRequest request) {

    LocalDate startDate = request.getStartDate();
    LocalDate endDate = startDate.withDayOfMonth(25);
    LocalDate previewStartDate = request.getPreviewStartDate();
    LocalDate previewEndDate = request.getPreviewEndDate();

    if (previewStartDate != null && previewEndDate != null) {
      validateDateRange(previewStartDate, previewEndDate);
      validateWithinCalendarPeriod(startDate, endDate, previewStartDate, previewEndDate);
    }

    return new OriginalCalendar(
        user,
        request.getTitle(),
        request.getDescription(),
        startDate,
        endDate,
        previewStartDate,
        previewEndDate,
        request.getVisibility(),
        request.getColor(),
        request.getCategory()
    );
  }

  private List<DistributedCalendar> createCalendarCopies(List<User> users, OriginalCalendar original) {
    List<DistributedCalendar> list = users.stream()
        .map(user -> DistributedCalendar.of(user, original))
        .toList();
    return calendarRepository.saveAll(list);
  }

  private void copyTasksBulk(Long originalCalendarId) {
    String sql = """
        INSERT INTO tasks (calendar_id, name, day_of_month, is_completed, created_at, updated_at)
        SELECT c.id, t.name, t.day_of_month, t.is_completed, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
        FROM tasks t
        JOIN calendar c
          ON c.original_calendar_id = t.calendar_id
        LEFT JOIN tasks existing
          ON existing.calendar_id = c.id
        WHERE t.calendar_id = ?
          AND existing.id IS NULL;
        """;
    jdbcTemplate.update(sql, originalCalendarId);
  }

  private void updateOriginalCalendar(OriginalCalendar originalCalendar,
                                      OriginalCalendarRequest request,
                                      LocalDate previewStartDate,
                                      LocalDate previewEndDate) {


    LocalDate startDate = request.getStartDate();
    LocalDate endDate = startDate.withDayOfMonth(25);

    if (previewStartDate != null && previewEndDate != null) {
      validateDateRange(previewStartDate, previewEndDate);
      validateWithinCalendarPeriod(startDate, endDate, previewStartDate, previewEndDate);
      originalCalendar.updatePreviewPeriod(previewStartDate, previewEndDate);
    }

    originalCalendar.updateTitle(request.getTitle());
    originalCalendar.updateDescription(request.getDescription());
    originalCalendar.updatePeriod(startDate, endDate);
    originalCalendar.updateVisibility(request.getVisibility());
    originalCalendar.updateColor(request.getColor());
    originalCalendar.updateCategory(request.getCategory());
  }

  private Map<Integer, DailyTaskStats> aggregateDailyTaskStats(List<Task> tasks) {
    return tasks.stream()
        .collect(Collectors.groupingBy(
            Task::getDay,
            TreeMap::new,
            Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                  int total = list.size();
                  int completed = (int) list.stream().filter(Task::isCompleted).count();
                  int pending = total - completed;
                  return new DailyTaskStats(total, completed, pending);
                }
            )
        ));
  }

  // DTO 변환
  private CalendarResponse toCalendarResponse(Calendar calendar, boolean isScrapable) {

    OfficialCalendar official = officialCalendarRepository.findByOriginalCalendarId(calendar.getId())
        .orElse(null);
    return CalendarResponse.from(calendar, official, isScrapable);
  }

  private List<CalendarResponse> toCalendarResponseList(List<Calendar> calendars, User viewer, User targetUser) {
    return calendars.stream()
        .filter(calendar -> canView(calendar, viewer, targetUser))
        .sorted(
            Comparator.comparing(Calendar::getStartDate)
                .thenComparingLong(Calendar::getId)
        )
        .map(calendar -> {
          boolean isScrapable = calendar.isScrapable() && !isCalendarOwner(viewer, calendar);
          return toCalendarResponse(calendar, isScrapable);
        })
        .toList();
  }

  private MonthlyCalendarStatsResponse toMonthlyAllCalendarStatsResponse(Calendar calendar, User viewer) {

    List<Task> tasks = taskRepository.findByCalendarIdAndDeletedAtIsNull(calendar.getId());
    tasks = filterMonthlyTasks(calendar, tasks, viewer);
    Map<Integer, DailyTaskStats> dayStatus = aggregateDailyTaskStats(tasks);

    CalendarType type = determineCalendarType(calendar);

    boolean isScrapable = type == CalendarType.ORIGINAL
        && calendar.isScrapable()
        && !isCalendarOwner(viewer, calendar);

    return MonthlyCalendarStatsResponse.from(calendar, type, isScrapable, dayStatus);
  }

  private MonthlyCalendarResponse toMonthlyCalendarStatsResponse(Calendar calendar, User viewer, boolean filtering) {

    List<Task> tasks = taskRepository.findByCalendarIdAndDeletedAtIsNull(calendar.getId());
    if (filtering) {
      tasks = filterMonthlyTasks(calendar, tasks, viewer);
    }
    Map<Integer, Integer> dayTaskCount = tasks.stream()
        .collect(Collectors.groupingBy(
            Task::getDay,
            TreeMap::new,
            Collectors.collectingAndThen(Collectors.toList(), List::size)
        ));

    CalendarType type = determineCalendarType(calendar);

    boolean isScrapable = type == CalendarType.ORIGINAL
        && calendar.isScrapable()
        && !isCalendarOwner(viewer, calendar);

    return MonthlyCalendarResponse.from(calendar, type, isScrapable, dayTaskCount);
  }

  private DailyTasksOfCalendarResponse toDailyTasksOfCalendarResponse(Calendar calendar,
                                                                      LocalDate date,
                                                                      User viewer,
                                                                      boolean filtering) {

    List<Task> tasks = taskRepository.findByCalendarIdAndDay(calendar.getId(), date.getDayOfMonth());
    if (filtering && !canViewDate(calendar, date, viewer)) {
      tasks = List.of();
    }
    List<TaskResponse> taskResponses = tasks.stream()
        .map(TaskResponse::from)
        .toList();

    CalendarType type = determineCalendarType(calendar);

    boolean isScrapable = type == CalendarType.ORIGINAL
        && calendar.isScrapable()
        && !isCalendarOwner(viewer, calendar);

    return DailyTasksOfCalendarResponse.from(calendar, type, date, isScrapable, taskResponses);
  }

}
