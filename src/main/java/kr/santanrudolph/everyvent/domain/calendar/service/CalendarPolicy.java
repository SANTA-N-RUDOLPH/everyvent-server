package kr.santanrudolph.everyvent.domain.calendar.service;


import kr.santanrudolph.everyvent.domain.calendar.Calendar;
import kr.santanrudolph.everyvent.domain.calendar.CalendarConstants;
import kr.santanrudolph.everyvent.domain.calendar.enums.CalendarType;
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


  // create
  public void validateCanCreate(User user, LocalDate startDate, CalendarType calendarType) {
    try {
      switch (calendarType) {
        case OFFICIAL -> {
          validateAdmin(user);
        }
        case PERSONAL -> {
          validateCalendarLimit(user, startDate);
          validateFuture(startDate);
        }
      }
    } catch (EveryventException e) {
      throw new EveryventException(e.getErrorCode(), "캘린더를 만들 수 없습니다. " + e.getDetail());
    }
  }


  // read
  public void validateCanView(User user, Calendar calendar) {
    switch (calendar.getCalendarType()) {
      case OFFICIAL -> {
        validateAdmin(user);
      }
      case PERSONAL -> {
        if (!canView(user, calendar)) {
          throw new EveryventException(ErrorCode.FORBIDDEN, "해당 캘린더에 접근 권한이 없습니다.");
        }
      }
    }
  }


  // update
  public void validateCanUpdate(User user, Calendar calendar) {
    try {
      validateUpdatePermission(user, calendar);
      validateFuture(calendar.getStartDate());
      validateDeleted(calendar);
      validateOriginalCalendar(calendar);
    } catch (EveryventException e) {
      throw new EveryventException(e.getErrorCode(), "캘린더를 수정할 수 없습니다. " + e.getDetail());
    }
  }

  public void validateCanUpdateScrapColor(User user, Calendar calendar) {
    try {
      // 이번달 캘린더일 때 update 가능
      validateThisMonth(calendar);
      validateDeleted(calendar);
      validateOwner(user, calendar);
      validateIsScrappedCalendar(calendar);
    } catch (EveryventException e) {
      throw new EveryventException(e.getErrorCode(), "캘린더 색상을 수정할 수 없습니다. " + e.getDetail());
    }
  }


  // delete
  public void validateCanSoftDelete(User user, Calendar calendar) {
    try {
      validateFuture(calendar.getStartDate());
      validateDeleted(calendar);
      validateOwner(user, calendar);
    } catch (EveryventException e) {
      throw new EveryventException(e.getErrorCode(), "캘린더를 삭제할 수 없습니다. " + e.getDetail());
    }
  }

  public void validateCanHardDelete(User user, Calendar calendar) {
    try {
      validateThisMonth(calendar);
      validateDeleted(calendar);
      validateOwner(user, calendar);
    } catch (EveryventException e) {
      throw new EveryventException(e.getErrorCode(), "캘린더를 삭제할 수 없습니다. " + e.getDetail());
    }
  }


  // scrap
  public void validateCanScrap(User user, Calendar calendar) {
    try {
      validateCanView(user, calendar);
      validateThisMonth(calendar);
      validateDeleted(calendar);
      validateNotOwner(user, calendar);
      validateNotAlreadyScrapped(user, calendar);
      validateCalendarLimit(user, calendar.getStartDate());
    } catch (EveryventException e) {
      throw new EveryventException(e.getErrorCode(), "캘린더를 스크랩할 수 없습니다. " + e.getDetail());
    }
  }

  public void validateCanCancelScrap(User user, Calendar calendar) {
    try {
      validateDeleted(calendar);
      validateThisMonth(calendar);
      validateOwner(user, calendar);
    } catch (EveryventException e) {
      throw new EveryventException(e.getErrorCode(), "스크랩을 취소할 수 없습니다. " + e.getDetail());
    }
  }


  // permission
  private void validateOwner(User user, Calendar calendar) {
    if (!isOwner(user, calendar)) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "본인 캘린더가 아닙니다.");
    }
  }

  private void validateUpdatePermission(User user, Calendar calendar) {
    if (calendar.isOfficialCalendar()) {
      validateAdmin(user);
    } else {
      validateOwner(user, calendar);
    }
  }

  private void validateNotOwner(User user, Calendar calendar) {
    if (isOwner(user, calendar)) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "본인 캘린더입니다.");
    }
  }

  private void validateAdmin(User user) {
    if (!isAdmin(user)) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "관리자가 아닙니다.");
    }
  }

  private boolean canView(User user, Calendar calendar) {
    if (isAdmin(user) || isOwner(user, calendar)) {
      return true;
    }

    Long viewerId = user.getId();
    Long ownerId = calendar.getUser().getId();

    return switch (calendar.getVisibility()) {
      case PUBLIC -> true;
      case FOLLOWER -> followService.isFollowing(viewerId, ownerId);
      case MUTUAL -> followService.isMutualFollow(viewerId, ownerId);
      case PRIVATE -> false;
      case ADMIN -> false;
    };
  }

  private boolean isAdmin(User user) {
    return user.getRole() == Role.ADMIN;
  }


  // calendarState
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

  private void validateFuture(LocalDate startDate) {
    YearMonth calendarYearMonth = YearMonth.from(startDate);
    YearMonth currentYearMonth = TimeUtil.currentYearMonth();

    if (calendarYearMonth.isAfter(currentYearMonth)) {
      return;
    }
    throw new EveryventException(ErrorCode.INVALID_INPUT, "과거입니다.");
  }

  private void validateThisMonth(Calendar calendar) {
    YearMonth calendarYearMonth = YearMonth.from(calendar.getStartDate());
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


  // scrap
  private void validateIsScrappedCalendar(Calendar calendar) {
    if (!calendar.getCalendarType().equals(CalendarType.SCRAPED)) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "스크랩한 캘린더가 아닙니다.");
    }
  }

  private void validateNotAlreadyScrapped(User user, Calendar calendar) {
    if (scrapService.isScrapped(user, calendar.getId())) {
      throw new EveryventException(ErrorCode.ALREADY_EXIST, "이미 스크랩한 캘린더입니다.");
    }
  }

}
