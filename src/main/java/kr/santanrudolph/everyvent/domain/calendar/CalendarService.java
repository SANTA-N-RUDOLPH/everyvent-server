package kr.santanrudolph.everyvent.domain.calendar;

import kr.santanrudolph.everyvent.domain.calendar.dto.request.OriginalCalendarRequest;
import kr.santanrudolph.everyvent.domain.calendar.dto.response.CalendarResponse;
import kr.santanrudolph.everyvent.domain.calendar.dto.response.OriginalCalendarResponse;
import kr.santanrudolph.everyvent.domain.user.Role;
import kr.santanrudolph.everyvent.domain.user.User;
import kr.santanrudolph.everyvent.domain.user.UserRepository;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import kr.santanrudolph.everyvent.global.util.EnumUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarService {

    private static final int CALENDAR_START_DAY = 1;
    private static final int CALENDAR_END_DAY = 25;

    private final CalendarRepository calendarRepository;
    private final UserRepository userRepository;

    @Transactional
    public OriginalCalendarResponse createCalendar(Long userId, OriginalCalendarRequest request, ZoneId userZone) {

        User user = getUserOrThrow(userId);

        boolean isOfficial = request.getIsOfficial() != null ? request.getIsOfficial() : false;
        validateOfficialCalendarPermission(isOfficial, user);

        // 한 달에 생성할 수 있는 캘린더 개수 제한 (최대 3개)
        validateMonthlyCalendarLimit(userId, request.getYear(), request.getMonth(), userZone);

        // 사용자 시간대 기준으로 캘린더 기간 계산
        InstantRange range = InstantRange.of(request.getYear(), request.getMonth(), userZone);

        OriginalCalendar calendar = buildCalendar(user, request, range, isOfficial, userZone);
        calendarRepository.save(calendar);

        return OriginalCalendarResponse.from(calendar, calendar.isScrapable(), userZone);
    }

    @Transactional
    public OriginalCalendarResponse createOfficialCalendar(Long adminId, OriginalCalendarRequest request, ZoneId userZone) {

        User admin = getUserOrThrow(adminId);
        validateAdminRole(admin);

        InstantRange range = InstantRange.of(request.getYear(), request.getMonth(), userZone);
        OriginalCalendar calendar = buildCalendar(admin, request, range, true, userZone);
        calendarRepository.save(calendar);

        return OriginalCalendarResponse.from(calendar, false, userZone);
    }

    public CalendarResponse getCalendar(Long calendarId, ZoneId userZone) {

        Calendar calendar = getActiveCalendarOrThrow(calendarId);
        return toCalendarResponse(calendar, calendar.isScrapable(), userZone);
    }

    public List<CalendarResponse> getMyCalendars(Long userId, int year, int month, ZoneId userZone) {

        User user = getUserOrThrow(userId);

        InstantRange range = InstantRange.of(year, month, userZone);
        log.info("조회 범위: {} ~ {}", range.start(), range.end());

        List<Calendar> calendars = calendarRepository.findAllByUserIdInMonth(user.getId(), range.start(), range.end());
        if (user.getRole() == Role.ADMIN) {
            // 관리자일 경우, 본인 캘린더 외에도 공식 캘린더를 함께 조회
            List<Calendar> officialCalendars = calendarRepository.findAllOfficialCalendars(user.getId(), range.start(), range.end(), Role.ADMIN);
            calendars.addAll(officialCalendars);
        }

        return calendars.stream()
                .map(c -> toCalendarResponse(c, c.isScrapable(), userZone))
                .collect(Collectors.toList());
    }

    public List<CalendarResponse> getCalendars(Long userId, Long targetId, int year, int month, ZoneId userZone) {

        User viewer = getUserOrThrow(userId);
        User targetUser = getUserOrThrow(targetId);

        InstantRange range = InstantRange.of(year, month, userZone);

        List<Calendar> calendars = calendarRepository.findAllByUserIdInMonth(
                targetUser.getId(), range.start(), range.end()
        );

        return calendars.stream()
                .filter(c -> canView(c, viewer, targetUser))
                .map(c -> {
                    boolean isScrapable = c.isScrapable() && !c.getUser().getId().equals(viewer.getId());
                    return toCalendarResponse(c, isScrapable, userZone);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public CalendarResponse updateCalendar(Long userId, Long calendarId, OriginalCalendarRequest request, ZoneId userZone) {

        Calendar calendar = getActiveCalendarOrThrow(calendarId);

        User viewer = getUserOrThrow(userId);
        validateUpdateOrDeletePermission(calendar, viewer);

        // 사용자 시간대 기준으로 캘린더 및 미리보기 기간 계산
        InstantRange range = InstantRange.of(request.getYear(), request.getMonth(), userZone);
        Instant previewStartDate = request.getPreviewStartDate() != null ? toStartOfDayOrNull(request.getPreviewStartDate(), userZone) : null;
        Instant previewEndDate = request.getPreviewEndDate() != null ? toEndOfDayOrNull(request.getPreviewEndDate(), userZone) : null;

        Visibility visibility = EnumUtil.parseEnum(Visibility.class, request.getVisibility());
        Category category  = EnumUtil.parseEnum(Category.class, request.getCategory());

        if (calendar instanceof OriginalCalendar oc) {
            updateOriginalCalendar(oc, request, range, visibility, category, previewStartDate, previewEndDate, viewer);
            return OriginalCalendarResponse.from(oc, oc.isScrapable(), userZone);
        }

        // TODO: 추후 ScrapCalendar 구현 시 추가
        // else if (calendar instanceof ScrapCalendar sc) { ... }

        throw new EveryventException(ErrorCode.INVALID_TYPE_VALUE);
    }

    @Transactional
    public void deleteCalendar(Long userId, Long calendarId) {

        Calendar calendar = getActiveCalendarOrThrow(calendarId);

        User viewer = getUserOrThrow(userId);
        validateUpdateOrDeletePermission(calendar, viewer);
        calendar.softDelete();

        calendarRepository.save(calendar);
    }

    @Transactional
    public void distributeOfficialCalendar(Long adminId, Long calendarId, ZoneId userZone) {

        User admin = getUserOrThrow(adminId);

        validateAdminRole(admin);

        Calendar calendar = getActiveCalendarOrThrow(calendarId);

        if (!(calendar instanceof OriginalCalendar originalCalendar)) {
            throw new EveryventException(ErrorCode.CALENDAR_TYPE_INVALID);
        }

        if (!originalCalendar.isOfficial()) {
            throw new EveryventException(ErrorCode.CALENDAR_NOT_OFFICIAL);
        }

        // 탈퇴하지 않은 일반 사용자 중, 아직 해당 공식 캘린더 복제본을 가지지 않은 대상에게 배포
        List<User> users = userRepository.findAllByRoleAndDeletedAtIsNull(Role.USER);
        List<Long> existingUserIds = calendarRepository.findUserIdsByOfficialId(originalCalendar.getId());

        List<OriginalCalendar> calendarsToSave = users.stream()
                .filter(user -> !existingUserIds.contains(user.getId()))
                .map(user -> createDistributedCalendar(user, originalCalendar))
                .collect(Collectors.toList());

        if (!calendarsToSave.isEmpty()) {
            calendarRepository.saveAll(calendarsToSave);
        }
    }

    // ==================== Private Helper Methods ====================

    private User getUserOrThrow(Long userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new EveryventException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateAdminRole(User user) {
        if (user.getRole() != Role.ADMIN) {
            throw new EveryventException(ErrorCode.ACCESS_DENIED);
        }
    }

    private Calendar getActiveCalendarOrThrow(Long calendarId) {
        Calendar calendar = calendarRepository.findById(calendarId)
                .orElseThrow(() -> new EveryventException(ErrorCode.CALENDAR_NOT_FOUND));

        if (calendar.getDeletedAt() != null) {
            throw new EveryventException(ErrorCode.CALENDAR_ALREADY_DELETED);
        }

        return calendar;
    }

    private void validateOfficialCalendarPermission(Boolean isOfficial, User user) {
        if (Boolean.TRUE.equals(isOfficial) && user.getRole() != Role.ADMIN) {
            throw new EveryventException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void validateMonthlyCalendarLimit(Long userId, int year, int month, ZoneId userZone) {
        InstantRange range = InstantRange.of(year, month, userZone);
        long userMonthlyCalendarCount = calendarRepository.countByUserIdInMonth(userId, range.start(), range.end());

        if (userMonthlyCalendarCount >= 3) {
            throw new EveryventException(ErrorCode.MAX_CALENDAR_EXCEEDED);
        }
    }

    private void validateUpdateOrDeletePermission(Calendar calendar, User viewer) {
        boolean isOwner = calendar.getUser().getId().equals(viewer.getId());
        boolean isAdminEditingOfficial = viewer.getRole() == Role.ADMIN
                && calendar instanceof OriginalCalendar oc
                && oc.isOfficial();

        if (!isOwner && !isAdminEditingOfficial) {
            throw new EveryventException(ErrorCode.CALENDAR_ACCESS_DENIED);
        }
    }

    private CalendarResponse toCalendarResponse(Calendar calendar, boolean isScrapable, ZoneId userZone) {
        if (calendar instanceof OriginalCalendar oc) {
            return OriginalCalendarResponse.from(oc, isScrapable, userZone);
        }
        // TODO: 추후 ScrapCalendar 구현 시 추가
        throw new EveryventException(ErrorCode.INVALID_TYPE_VALUE);
    }

    private void updateOriginalCalendar(OriginalCalendar oc, OriginalCalendarRequest request,
                                        InstantRange range, Visibility visibility, Category category,
                                        Instant previewStartDate, Instant previewEndDate, User viewer) {
        oc.ensureOfficialEditableByUser();
        oc.updateDetails(request.getTitle(), request.getDescription(), range.start(), range.end(), category);
        oc.changeColor(request.getColor());
        oc.changeVisibility(visibility);
        oc.setPreviewPeriod(previewStartDate, previewEndDate);

        boolean isOfficial = request.getIsOfficial() != null ? request.getIsOfficial() : false;
        if (viewer.getRole() == Role.ADMIN) {
            oc.changeOfficialStatus(isOfficial);
        }
    }

    private OriginalCalendar createDistributedCalendar(User user, OriginalCalendar calendar) {

        OriginalCalendar userCalendar = OriginalCalendar.builder()
                .user(user)
                .title(calendar.getTitle())
                .description(calendar.getDescription())
                .startDate(calendar.getStartDate())
                .endDate(calendar.getEndDate())
                .visibility(calendar.getVisibility())
                .color(calendar.getColor())
                .category(calendar.getCategory())
                .isOfficial(false)
                .build();

        if (calendar.getPreviewStartDate() != null && calendar.getPreviewEndDate() != null) {
            userCalendar.setPreviewPeriod(calendar.getPreviewStartDate(), calendar.getPreviewEndDate());
        }
        userCalendar.enterOfficialId(calendar.getId());

        return userCalendar;
    }

    private OriginalCalendar buildCalendar(User user, OriginalCalendarRequest request, InstantRange range,
                                           boolean isOfficial, ZoneId userZone) {

        Visibility visibility = EnumUtil.parseEnum(Visibility.class, request.getVisibility());
        Category category  = EnumUtil.parseEnum(Category.class, request.getCategory());

        Instant previewStartDate = request.getPreviewStartDate() != null ? toStartOfDayOrNull(request.getPreviewStartDate(), userZone) : null;
        Instant previewEndDate = request.getPreviewEndDate() != null ? toEndOfDayOrNull(request.getPreviewEndDate(), userZone) : null;

        OriginalCalendar calendar = OriginalCalendar.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .startDate(range.start())
                .endDate(range.end())
                .visibility(visibility)
                .color(request.getColor())
                .category(category)
                .isOfficial(isOfficial)
                .build();

        calendar.setPreviewPeriod(previewStartDate, previewEndDate);

        return calendar;
    }

    private static Instant toStartOfDayOrNull(LocalDate date, ZoneId userZone) {
        return date != null ? date.atStartOfDay(userZone).toInstant() : null;
    }

    private static Instant toEndOfDayOrNull(LocalDate date, ZoneId userZone) {
        return date != null ? date.atTime(LocalTime.MAX).atZone(userZone).toInstant() : null;
    }

    private static class InstantRange {
        private final Instant start;
        private final Instant end;

        private InstantRange(Instant start, Instant end) {
            this.start = start;
            this.end = end;
        }

        public static InstantRange of(int year, int month, ZoneId userZone) {
            LocalDate startLocal = LocalDate.of(year, month, CALENDAR_START_DAY);
            LocalDate endLocal = LocalDate.of(year, month, CALENDAR_END_DAY);
            return new InstantRange(
                    toStartOfDayOrNull(startLocal, userZone),
                    toStartOfDayOrNull(endLocal.plusDays(1), userZone)
            );
        }

        public Instant start() { return start; }
        public Instant end() { return end; }
    }

    private boolean canView(Calendar calendar, User viewer, User targetUser) {
        return switch (calendar.getVisibility()) {
            case PUBLIC -> true;
            case PRIVATE -> viewer.getId().equals(targetUser.getId());
            // TODO: 추후 Follower 구현 시 추가
            // case MUTUAL -> {...}
            // case FOLLOWER -> {...}
            default -> false;
        };
    }
}
