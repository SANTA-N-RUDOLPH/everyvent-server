package kr.santanrudolph.everyvent.domain.calendar;

import kr.santanrudolph.everyvent.domain.calendar.dto.request.OriginalCalendarRequest;
import kr.santanrudolph.everyvent.domain.calendar.dto.response.CalendarResponse;
import kr.santanrudolph.everyvent.domain.calendar.dto.response.DistributedCalendarResponse;
import kr.santanrudolph.everyvent.domain.calendar.dto.response.OfficialCalendarResponse;
import kr.santanrudolph.everyvent.domain.calendar.dto.response.OriginalCalendarResponse;
import kr.santanrudolph.everyvent.domain.calendar.entity.Calendar;
import kr.santanrudolph.everyvent.domain.calendar.entity.DistributedCalendar;
import kr.santanrudolph.everyvent.domain.calendar.entity.OfficialCalendar;
import kr.santanrudolph.everyvent.domain.calendar.entity.OriginalCalendar;
import kr.santanrudolph.everyvent.domain.follow.FollowService;
import kr.santanrudolph.everyvent.domain.user.enums.Role;
import kr.santanrudolph.everyvent.domain.user.User;
import kr.santanrudolph.everyvent.domain.user.UserRepository;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    OriginalCalendar originalCalendar = officialCalendar.getOriginalCalendar();
    validateDistribution(officialCalendar);

    // 탈퇴하지 않은 사용자 중, 아직 해당 공식 캘린더 복제본을 가지지 않은 대상에게 배포
    List<User> targetUsers = userRepository.findTargetUsersForDistribution(originalCalendar.getId());

    List<DistributedCalendar> calendarsToSave = targetUsers.stream()
            .map(user -> DistributedCalendar.of(user, originalCalendar))
            .collect(Collectors.toList());

    List<DistributedCalendar> savedCalendars = calendarRepository.saveAll(calendarsToSave);
    officialCalendar.updateDistributedAt(Instant.now());

    return savedCalendars.stream()
        .map(DistributedCalendarResponse::from)
        .collect(Collectors.toList());
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

  public List<CalendarResponse> getMyCalendars(Long userId, int year, int month) {

    User user = getUserOrThrow(userId);

    LocalDate date = LocalDate.of(year, month, 1);
    log.info("조회 캘린더 기간 시작일: {}", date);

    List<Calendar> calendars = calendarRepository.findOriginalCalendarsByUserIdInMonth(user.getId(), date);

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

  public List<CalendarResponse> getOfficialCalendars(int year, int month, Long adminId) {

    User admin = getUserOrThrow(adminId);
    validateAdminRole(admin);

    LocalDate date = LocalDate.of(year, month, 1);
    List<OfficialCalendar> officialEntities = officialCalendarRepository.findAllByPeriod(date);
    List<Calendar> officialCalendars = officialEntities.stream()
        .map(OfficialCalendar::getOriginalCalendar)
        .collect(Collectors.toList());

    return toCalendarResponseList(officialCalendars, admin, admin);
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

  // TODO: 추후 ScrapCalendar 구현 시, updateScrapedCalendar 추가

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

    calendar.softDelete();
    calendarRepository.save(calendar);
  }

  @Transactional
  public void deleteOfficialCalendar(Long adminId, Long calendarId) {

    OfficialCalendar official = getOfficialCalendarOrThrow(calendarId, adminId);
    OriginalCalendar calendar = official.getOriginalCalendar();

    calendar.softDelete();
    official.softDelete();

    calendarRepository.save(calendar);
    officialCalendarRepository.save(official);
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
      throw new EveryventException(ErrorCode.FORBIDDEN, "이미 삭제된 캘린더입니다.");
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
      throw new EveryventException(ErrorCode.FORBIDDEN, "삭제된 원본 캘린더엔 더 이상 접근할 수 없습니다.");
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
    return Objects.equals(calendar.getUser().getId(), user.getId());
  }

  private boolean canView(Calendar calendar, User viewer, User targetUser) {

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
        request.getVisibility(),
        request.getColor(),
        request.getCategory(),
        previewStartDate,
        previewEndDate
    );
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

  // DTO 변환
  private CalendarResponse toCalendarResponse(Calendar calendar, boolean isScrapable) {

    OfficialCalendar official = officialCalendarRepository.findByOriginalCalendarId(calendar.getId())
        .orElse(null);
    return CalendarResponse.from(calendar, official, isScrapable);
  }

  private List<CalendarResponse> toCalendarResponseList(List<Calendar> calendars, User viewer, User targetUser) {
    return calendars.stream()
        .filter(calendar -> canView(calendar, viewer, targetUser))
        .sorted(Comparator.comparingLong(Calendar::getId))
        .map(calendar -> {
          boolean isScrapable = calendar.isScrapable() && !isCalendarOwner(viewer, calendar);
          return toCalendarResponse(calendar, isScrapable);
        })
        .toList();
  }

}
