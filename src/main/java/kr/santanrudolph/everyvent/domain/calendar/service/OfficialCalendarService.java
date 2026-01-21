package kr.santanrudolph.everyvent.domain.calendar.service;

import kr.santanrudolph.everyvent.domain.calendar.Calendar;
import kr.santanrudolph.everyvent.domain.calendar.Official;
import kr.santanrudolph.everyvent.domain.calendar.dto.reponse.OfficialCalendarResponse;
import kr.santanrudolph.everyvent.domain.calendar.dto.request.CalendarCreateRequest;
import kr.santanrudolph.everyvent.domain.calendar.dto.request.CalendarUpdateRequest;
import kr.santanrudolph.everyvent.domain.calendar.dto.request.OfficialCalendarCreateRequest;
import kr.santanrudolph.everyvent.domain.calendar.dto.request.OfficialCalendarUpdateRequest;
import kr.santanrudolph.everyvent.domain.calendar.enums.CalendarType;
import kr.santanrudolph.everyvent.domain.calendar.repository.OfficialRepository;
import kr.santanrudolph.everyvent.domain.user.User;
import kr.santanrudolph.everyvent.domain.user.enums.Role;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OfficialCalendarService {

  private final OfficialRepository officialRepository;
  private final CalendarService calendarService;

  // create
  @Transactional
  public OfficialCalendarResponse createOfficialCalendar(OfficialCalendarCreateRequest request) {
    User user = calendarService.getCurrentUser();
    validateAdmin(user);

    CalendarCreateRequest calendarCreateRequest = request.toCalendarCreateRequest();
    Calendar saved = calendarService.createCalendar(calendarCreateRequest, CalendarType.OFFICIAL);
    Official official = Official.create(saved);
    officialRepository.save(official);

    log.info("Official calendar created - calendarId={}, userId={}, startDate={}",
        saved.getId(), user.getId(), saved.getStartDate());

    return OfficialCalendarResponse.from(saved, official);
  }


  // read
  public Calendar getOfficialCalendar(Long calendarId) {
    User user = calendarService.getCurrentUser();
    validateAdmin(user);
    Calendar calendar = calendarService.findCalendarByIdAndDeletedAtIsNull(calendarId);
    validateOfficialCalendar(calendar);
    return calendar;
  }

  public Official getOfficial(Long calendarId) {
    return officialRepository.findById(calendarId).orElseThrow(() ->
        new EveryventException(ErrorCode.NOT_FOUND, "존재하지 않는 공식캘린더입니다.")
    );
  }

  public List<OfficialCalendarResponse> getOfficialCalendars() {
    User user = calendarService.getCurrentUser();
    validateAdmin(user);

    return officialRepository.findAllWithCalendar().stream()
        .map(official -> OfficialCalendarResponse.from(official.getCalendar(), official))
        .toList();
  }

  // update
  @Transactional
  public OfficialCalendarResponse updateOfficialCalendar(Long calendarId, OfficialCalendarUpdateRequest request) {
    User user = calendarService.getCurrentUser();
    validateAdmin(user);
    Calendar officialCalendar = getOfficialCalendar(calendarId);
    Official official = getOfficial(calendarId);

    CalendarUpdateRequest calendarUpdateRequest = request.toCalendarUpdateRequest();
    calendarService.updateCalendar(officialCalendar, calendarUpdateRequest);

    log.info("Official calendar updated - calendarId={}, userId={}, title={}",
        calendarId, user.getId(), officialCalendar.getTitle());

    return OfficialCalendarResponse.from(officialCalendar, official);
  }

  // delete
  @Transactional
  public void deleteOfficialCalendar(Long calendarId) {
    User user = calendarService.getCurrentUser();
    validateAdmin(user);
    Official official = getOfficial(calendarId);
    officialRepository.delete(official);

    log.info("Official calendar deleted - calendarId={}, userId={}", calendarId, user.getId());
  }

  // todo: distribute

  private void validateAdmin(User user) {
    if (user.getRole() != Role.ADMIN) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "관리자가 아닙니다.");
    }
  }

  private void validateOfficialCalendar(Calendar calendar) {
    if (calendar.getCalendarType() != CalendarType.OFFICIAL) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "공식 캘린더가 아닙니다.");
    }
  }

}

