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
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarService {

  private final CalendarRepository calendarRepository;
  private final OfficialCalendarRepository officialCalendarRepository;
  private final UserRepository userRepository;

  @Transactional
  public OriginalCalendarResponse createCalendar(Long userId, OriginalCalendarRequest request) {

    User user = getUserOrThrow(userId);

    // 한 달에 생성할 수 있는 캘린더 개수 제한 (최대 3개)
    validateMonthlyCalendarLimit(userId, request.getYear(), request.getMonth());

    // 사용자 시간대 기준으로 캘린더 기간 계산
    InstantRange range = InstantRange.of(request.getYear(), request.getMonth());
    log.info("캘린더 기간 범위: {} ~ {}", range.start(), range.end());
    log.info("미리보기 기간 범위: {} ~ {}", request.getPreviewStartDate(), request.getPreviewEndDate());

    OriginalCalendar calendar = buildCalendar(user, request, range);
    calendarRepository.save(calendar);

    return OriginalCalendarResponse.from(calendar, calendar.isScrapable());
  }

  @Transactional
  public OfficialCalendarResponse createOfficialCalendar(Long adminId, OriginalCalendarRequest request) {

    User admin = getUserOrThrow(adminId);
    validateAdminRole(admin);

    InstantRange range = InstantRange.of(request.getYear(), request.getMonth());
    OriginalCalendar calendar = buildCalendar(admin, request, range);
    calendarRepository.save(calendar);

    OfficialCalendar officialCalendar = new OfficialCalendar(calendar, null);
    officialCalendarRepository.save(officialCalendar);

    return OfficialCalendarResponse.from(officialCalendar);
  }

  @Transactional
  public void distributeOfficialCalendar(Long adminId, Long calendarId) {

    User admin = getUserOrThrow(adminId);
    validateAdminRole(admin);

    OfficialCalendar officialCalendar = officialCalendarRepository
            .findByOriginalCalendarId(calendarId)
            .orElseThrow(() -> new EveryventException(ErrorCode.INVALID_INPUT, "해당 캘린더는 공식 캘린더가 아닙니다."));

    Calendar calendar = getActiveCalendarOrThrow(officialCalendar.getOriginalCalendar().getId());
    if (!(calendar instanceof OriginalCalendar originalCalendar)) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "해당 캘린더는 원본 캘린더가 아닙니다.");
    }

    // 탈퇴하지 않은 일반 사용자 중, 아직 해당 공식 캘린더 복제본을 가지지 않은 대상에게 배포
    List<User> targetUsers = userRepository.findTargetUsersForDistribution(Role.USER, originalCalendar.getId());

    List<DistributedCalendar> calendarsToSave = targetUsers.stream()
            .map(user -> DistributedCalendar.of(user, originalCalendar))
            .collect(Collectors.toList());

    calendarRepository.saveAll(calendarsToSave);
    officialCalendar.updateDistributedAt(Instant.now());
  }

  public CalendarResponse getCalendar(Long calendarId, Long userId) {

    User viewer = getUserOrThrow(userId);
    Calendar calendar = getActiveCalendarOrThrow(calendarId);

    if (!canView(calendar, viewer, calendar.getUser())) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "해당 캘린더를 조회할 권한이 없습니다.");
    }

    boolean isScrapable = calendar.isScrapable() && !calendar.getUser().getId().equals(viewer.getId());
    return toCalendarResponse(calendar, isScrapable);
  }

  public List<CalendarResponse> getMyCalendars(Long userId, int year, int month) {

    User user = getUserOrThrow(userId);

    InstantRange range = InstantRange.of(year, month);
    log.info("조회 범위: {} ~ {}", range.start(), range.end());

    List<Calendar> calendars = calendarRepository.findAllByUserIdInMonth(user.getId(), range.start(), range.end());

    if (user.getRole() == Role.ADMIN) {
      // 관리자일 경우, 본인 캘린더 외에도 공식 캘린더를 함께 조회
      List<OfficialCalendar> officialEntities = officialCalendarRepository.findAllByPeriod(range.start(), range.end());
      List<Calendar> officialCalendars = officialEntities.stream()
              .map(OfficialCalendar::getOriginalCalendar)
              .collect(Collectors.toList());

      calendars = Stream.concat(calendars.stream(), officialCalendars.stream()).distinct().toList();
    }

    return toCalendarResponseList(calendars, user, user);
  }

  public List<CalendarResponse> getCalendars(Long userId, Long targetId, int year, int month) {

    User viewer = getUserOrThrow(userId);
    User targetUser = getUserOrThrow(targetId);

    InstantRange range = InstantRange.of(year, month);

    List<Calendar> calendars = calendarRepository.findAllByUserIdInMonth(targetUser.getId(), range.start(), range.end());

    return toCalendarResponseList(calendars, viewer, targetUser);
  }

  @Transactional
  public CalendarResponse updateCalendar(Long userId, Long calendarId, OriginalCalendarRequest request) {

    Calendar calendar = getActiveCalendarOrThrow(calendarId);

    User viewer = getUserOrThrow(userId);
    validateUpdateOrDeletePermission(calendar, viewer, true);

    // 사용자 시간대 기준으로 캘린더 및 미리보기 기간 계산
    InstantRange range = InstantRange.of(request.getYear(), request.getMonth());

    ZoneId koreaZone = ZoneId.of("Asia/Seoul");
    Instant previewStartDate = request.getPreviewStartInstant(koreaZone);
    Instant previewEndDate = request.getPreviewEndInstant(koreaZone);

    if (calendar instanceof OriginalCalendar oc) {
      updateOriginalCalendar(oc, request, range, previewStartDate, previewEndDate, viewer);
    }

    // TODO: 추후 ScrapCalendar 구현 시 추가
    // else if (calendar instanceof ScrapCalendar sc) { ... }

    return toCalendarResponse(calendar, calendar.isScrapable());
  }

  @Transactional
  public void deleteCalendar(Long userId, Long calendarId) {

    Calendar calendar = getActiveCalendarOrThrow(calendarId);

    User viewer = getUserOrThrow(userId);
    validateUpdateOrDeletePermission(calendar, viewer, false);

    if (calendar instanceof OriginalCalendar oc) {
      oc.softDelete();
      calendarRepository.save(oc);
      officialCalendarRepository.findByOriginalCalendarId(oc.getId())
              .ifPresent(official -> {
                official.softDelete();
                officialCalendarRepository.save(official);
              });
    } else {
      calendar.softDelete();
      calendarRepository.save(calendar);
    }
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
      throw new EveryventException(ErrorCode.INVALID_INPUT, "이미 삭제된 캘린더입니다.");
    }

    return calendar;
  }

  // 권한/검증 관련
  private void validateAdminRole(User user) {
    if (user.getRole() != Role.ADMIN) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "공식 캘린더에 접근할 권한이 없습니다.");
    }
  }

  private void validateUpdateOrDeletePermission(Calendar calendar, User viewer, boolean isUpdate) {
    boolean isAdmin = viewer.getRole() == Role.ADMIN;
    boolean isOwner = Objects.equals(calendar.getUser().getId(), viewer.getId());
    boolean isOriginal = calendar instanceof OriginalCalendar;
    boolean isOfficialOriginal = false;
    if (isOriginal) {
      isOfficialOriginal = officialCalendarRepository.existsByOriginalCalendar((OriginalCalendar) calendar);
    }

    if (isUpdate) {
      if (isOfficialOriginal && isAdmin) return;
      if (isOriginal && isOwner) return;
      throw new EveryventException(ErrorCode.FORBIDDEN, "해당 캘린더 수정 권한이 없습니다.");
    }

    if (!isOwner && !isAdmin) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "해당 캘린더 삭제 권한이 없습니다.");
    }
  }

  private boolean canView(Calendar calendar, User viewer, User targetUser) {
    return switch (calendar.getVisibility()) {
      case PUBLIC -> true;
      case PRIVATE -> viewer.getId().equals(targetUser.getId());
      default -> false;
    };
  }

  // 생성/업데이트 관련
  private OriginalCalendar buildCalendar(User user,
                                         OriginalCalendarRequest request,
                                         InstantRange range) {

    ZoneId koreaZone = ZoneId.of("Asia/Seoul");
    Instant previewStartDate = request.getPreviewStartInstant(koreaZone);
    Instant previewEndDate = request.getPreviewEndInstant(koreaZone);

    if (previewStartDate != null && previewEndDate != null) {
      validateDateRange(previewStartDate, previewEndDate);
      validateWithinCalendarPeriod(range, previewStartDate, previewEndDate);
    }

    return new OriginalCalendar(
            user,
            request.getTitle(),
            request.getDescription(),
            range.start(),
            range.end(),
            request.getVisibility(),
            request.getColor(),
            request.getCategory(),
            previewStartDate,
            previewEndDate
    );
  }

  private void updateOriginalCalendar(OriginalCalendar oc, OriginalCalendarRequest request,
                                      InstantRange range,
                                      Instant previewStartDate, Instant previewEndDate, User viewer) {

    if (previewStartDate != null && previewEndDate != null) {
      validateDateRange(previewStartDate, previewEndDate);
      validateWithinCalendarPeriod(range, previewStartDate, previewEndDate);
      oc.updatePreviewPeriod(previewStartDate, previewEndDate);
    }

    oc.updateTitle(request.getTitle());
    oc.updateDescription(request.getDescription());
    oc.updatePeriod(range.start(), range.end());
    oc.updateVisibility(request.getVisibility());
    oc.updateColor(request.getColor());
    oc.updateCategory(request.getCategory());

    if (viewer.getRole() == Role.ADMIN) {
      handleOfficialStatus(oc, request.getIsOfficial());
    }
  }

  private void handleOfficialStatus(OriginalCalendar oc, Boolean isOfficial) {
    if (isOfficial == null) return;

    boolean exists = officialCalendarRepository.existsByOriginalCalendar(oc);

    if (isOfficial && !exists) {
      OfficialCalendar ocEntity = new OfficialCalendar(oc, null);
      officialCalendarRepository.save(ocEntity);
    }

    if (!isOfficial && exists) {
      officialCalendarRepository.deleteByOriginalCalendar(oc);
    }
  }

  // 검증 관련
  private void validateMonthlyCalendarLimit(Long userId, int year, int month) {
    InstantRange range = InstantRange.of(year, month);
    long userMonthlyCalendarCount = calendarRepository.countByUserIdInMonth(userId, range.start(), range.end());

    if (userMonthlyCalendarCount >= 3) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "캘린더는 최대 3개까지 생성할 수 있습니다.");
    }
  }

  private void validateDateRange(Instant startDate, Instant endDate) {
    if (startDate.isAfter(endDate)) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "미리보기 기간의 시작일은 종료일보다 앞서야 합니다.");
    }
  }

  private void validateWithinCalendarPeriod(InstantRange range, Instant start, Instant end) {
    if (start.isBefore(range.start()) || end.isAfter(range.end())) {
      throw new EveryventException(ErrorCode.INVALID_INPUT,
              "미리보기 기간은 캘린더 기간 안에 포함되어야 합니다.");
    }
  }

  // DTO 변환
  private CalendarResponse toCalendarResponse(Calendar calendar, boolean isScrapable) {
    if (calendar instanceof OriginalCalendar oc) {

      Optional<OfficialCalendar> officialOpt =
              officialCalendarRepository.findByOriginalCalendarId(oc.getId());
      if (officialOpt.isPresent()) {
        return OfficialCalendarResponse.from(officialOpt.get());
      }

      return OriginalCalendarResponse.from(oc, isScrapable);
    } else if (calendar instanceof DistributedCalendar dc) {
      return DistributedCalendarResponse.from(dc);
    }
    throw new EveryventException(ErrorCode.INVALID_INPUT, "지원하지 않는 캘린더 타입입니다.");
  }

  private List<CalendarResponse> toCalendarResponseList(List<Calendar> calendars, User viewer, User targetUser) {
    return calendars.stream()
            .filter(c -> canView(c, viewer, targetUser))
            .sorted(Comparator.comparingLong(Calendar::getId))
            .map(c -> {
              boolean isScrapable = c.isScrapable() && !c.getUser().getId().equals(viewer.getId());
              return toCalendarResponse(c, isScrapable);
            })
            .toList();
  }

}
