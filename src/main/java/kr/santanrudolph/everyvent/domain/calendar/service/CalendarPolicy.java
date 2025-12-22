package kr.santanrudolph.everyvent.domain.calendar.service;


import kr.santanrudolph.everyvent.domain.calendar.Calendar;
import kr.santanrudolph.everyvent.domain.calendar.CalendarConstants;
import kr.santanrudolph.everyvent.domain.calendar.enums.CalendarType;
import kr.santanrudolph.everyvent.domain.calendar.enums.Visibility;
import kr.santanrudolph.everyvent.domain.calendar.repository.CalendarRepository;
import kr.santanrudolph.everyvent.domain.follow.FollowService;
import kr.santanrudolph.everyvent.domain.user.User;
import kr.santanrudolph.everyvent.domain.user.enums.Role;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import kr.santanrudolph.everyvent.global.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;

@Component
@RequiredArgsConstructor
public class CalendarPolicy {
  private final CalendarRepository calendarRepository;
  private final FollowService followService;
  private final ScrapService scrapService;

  public boolean canView(User user, Calendar calendar) {
    if (user.getRole() == Role.ADMIN) {
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

  public boolean canScrap(User user, Calendar calendar) {
    if (isOwner(user, calendar)) {
      return false;
    }

    if (!canView(user, calendar)) {
      return false;
    }

    return !scrapService.isScrapped(user, calendar.getId());
  }

  public boolean isOwner(User user, Calendar calendar) {
    return calendar.getUser().getId().equals(user.getId());
  }

  public void validateCanCreate(User user, LocalDate startDate) {
    try {
      validateCalendarLimit(user, startDate);
      validateFuture(startDate);
    } catch (EveryventException e) {
      throw new EveryventException(e.getErrorCode(), "캘린더를 만들 수 없습니다. " + e.getMessage());
    }
  }

  public void validateCanUpdate(User user, Calendar calendar) {
    try {
      validateFuture(calendar.getStartDate());
      validateDeleted(calendar);
      validateOwner(user, calendar);
      validateOriginalCalendar(calendar);
    } catch (EveryventException e) {
      throw new EveryventException(e.getErrorCode(), "캘린더를 수정할 수 없습니다. " + e.getMessage());
    }
  }

  public void validateCanUpdateScrapColor(User user, Calendar calendar) {
    try {
      // 이번달 캘린더일 때 update 가능
      validateThisMonth(calendar.getStartDate());
      validateDeleted(calendar);
      validateOwner(user, calendar);
      validateIsScrappedCalendar(calendar);
    } catch (EveryventException e) {
      throw new EveryventException(e.getErrorCode(), "캘린더 색상을 수정할 수 없습니다. " + e.getMessage());
    }
  }

  public void validateCanDelete(User user, Calendar calendar) {
    try {
      validateFuture(calendar.getStartDate());
      validateDeleted(calendar);
      validateOwner(user, calendar);
    } catch (EveryventException e) {
      throw new EveryventException(e.getErrorCode(), "캘린더를 삭제할 수 없습니다." + e.getMessage());
    }
  }

  public void validateCanView(User user, Calendar calendar) {
    if (!canView(user, calendar)) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "해당 캘린더에 접근 권한이 없습니다.");
    }
  }

  public void validateCanScrap(User user, Calendar calendar) {
    try {
      validateDeleted(calendar);
      validateNotOwner(user, calendar);
      validateNotAlreadyScrapped(user, calendar);
      validateCanView(user, calendar);
    } catch (EveryventException e) {
      throw new EveryventException(e.getErrorCode(), "캘린더를 스크랩할 수 없습니다. " + e.getMessage());
    }
  }

  public void validateCanCancelScrap(User user, Calendar calendar) {
    try {
      validateDeleted(calendar);
      validateAlreadyScrapped(user, calendar);
    } catch (EveryventException e) {
      throw new EveryventException(e.getErrorCode(), "스크랩을 취소할 수 없습니다. " + e.getMessage());
    }
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

  public void validateOwner(User user, Calendar calendar) {
    if (!isOwner(user, calendar)) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "본인 캘린더가 아닙니다.");
    }
  }

  private void validateFuture(LocalDate startDate) {
    YearMonth calendarYearMonth = YearMonth.from(startDate);
    YearMonth currentYearMonth = TimeUtil.currentYearMonth();

    if (calendarYearMonth.isAfter(currentYearMonth)) {
      return;
    }
    throw new EveryventException(ErrorCode.INVALID_INPUT, "과거입니다.");
  }

  private void validateThisMonth(LocalDate startDate) {
    YearMonth calendarYearMonth = YearMonth.from(startDate);
    YearMonth currentYearMonth = TimeUtil.currentYearMonth();

    if (calendarYearMonth.isAfter(currentYearMonth)) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "미래입니다.");
    } else if (calendarYearMonth.isBefore(currentYearMonth)) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "과거입니다.");
    }
  }

  private void validateDeleted(Calendar calendar) {
    if (calendar.isDeleted()) {
      throw new EveryventException(ErrorCode.NOT_FOUND, "삭제된 캘린더입니다.");
    }
  }

  private void validateOriginalCalendar(Calendar calendar) {
    if (!calendar.isOriginalCalendar()) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "원본 캘린더가 아닙니다.");
    }
  }

  private void validateIsScrappedCalendar(Calendar calendar) {
    if (!calendar.getCalendarType().equals(CalendarType.SCRAPED)) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "스크랩한 캘린더가 아닙니다.");
    }
  }

  private void validateNotOwner(User user, Calendar calendar) {
    if (isOwner(user, calendar)) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "본인 캘린더입니다.");
    }
  }

  private void validateNotAlreadyScrapped(User user, Calendar calendar) {
    if (scrapService.isScrapped(user, calendar.getId())) {
      throw new EveryventException(ErrorCode.ALREADY_EXIST, "이미 스크랩한 캘린더입니다.");
    }
  }

  private void validateAlreadyScrapped(User user, Calendar calendar) {
    if (!scrapService.isScrapped(user, calendar.getId())) {
      throw new EveryventException(ErrorCode.NOT_FOUND, "스크랩하지 않은 캘린더입니다.");
    }
  }

}
