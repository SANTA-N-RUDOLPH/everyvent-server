package kr.santanrudolph.everyvent.domain.calendar;

import jakarta.persistence.*;
import kr.santanrudolph.everyvent.domain.calendar.enums.CalendarColor;
import kr.santanrudolph.everyvent.domain.calendar.enums.CalendarType;
import kr.santanrudolph.everyvent.domain.calendar.enums.Category;
import kr.santanrudolph.everyvent.domain.calendar.enums.Visibility;
import kr.santanrudolph.everyvent.domain.user.User;
import kr.santanrudolph.everyvent.global.entity.BaseEntity;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

import static kr.santanrudolph.everyvent.domain.calendar.CalendarConstants.DEFAULT_PERIOD_DAYS;

// 캘린더는 1달 단위로만 생성 가능하다.
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Calendar extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @Column(nullable = false)
  private String title;

  private String description;

  @Column(nullable = false)
  private LocalDate startDate;

  @Column(nullable = false)
  private LocalDate endDate;

  @Column(nullable = true)
  private LocalDate previewStartDay;

  @Column(nullable = true)
  private LocalDate previewEndDay;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Visibility visibility;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Category category;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private CalendarColor color;

  // null이면 원본 캘린더
  @Column(nullable = true)
  private Long originalCalendarId;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private CalendarType calendarType;

  @Column(nullable = true)
  Instant deletedAt;

  public boolean isOriginalCalendar() {
    return originalCalendarId == null;
  }

  public boolean isScrappable() {
    return calendarType == CalendarType.PERSONAL && visibility == Visibility.PUBLIC;
  }

  public boolean isOfficialCalendar() {
    return calendarType == CalendarType.OFFICIAL;
  }

  public boolean isDeleted() {
    return deletedAt != null;
  }

  public static Calendar createCalendar(User user, String title, String description, LocalDate startDate,
                                        LocalDate endDate, Visibility visibility, Category category,
                                        CalendarColor color, Long originalCalendarId, CalendarType calendarType,
                                        LocalDate previewStartDay, LocalDate previewEndDay) {

    Calendar calendar = new Calendar();
    calendar.user = user;
    calendar.title = title;
    calendar.description = description;
    calendar.startDate = startDate;
    calendar.endDate = endDate;
    calendar.previewStartDay = previewStartDay;
    calendar.previewEndDay = previewEndDay;
    calendar.visibility = visibility;
    calendar.category = category;
    calendar.color = color;
    calendar.originalCalendarId = originalCalendarId;
    calendar.calendarType = calendarType;

    return calendar;
  }

  public static Calendar createCalendarWithStartDay(User user, String title, String description,
                                                    LocalDate startDate, Visibility visibility,
                                                    CalendarColor color, Category category, CalendarType calendarType) {

    LocalDate endDate = startDate.plusDays(DEFAULT_PERIOD_DAYS);
    return createCalendar(
        user,
        title,
        description,
        startDate,
        endDate,
        visibility,
        category,
        color,
        null,
        calendarType,
        null,
        null
    );
  }

  public static Calendar createCalendarWithStartAndEndDay(User user, String title, String description,
                                                          LocalDate startDate, LocalDate endDate,
                                                          Visibility visibility, CalendarColor color,
                                                          Category category, CalendarType calendarType) {

    return createCalendar(
        user,
        title,
        description,
        startDate,
        endDate,
        visibility,
        category,
        color,
        null,
        calendarType,
        null,
        null);
  }

  public static Calendar createScrappedCalendar(User user, Calendar originalCalendar, CalendarColor color) {

    if (!originalCalendar.isScrappable()) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "스크랩할 수 없습니다.");
    }

    return createCalendar(
        user,
        originalCalendar.getTitle(),
        originalCalendar.getDescription(),
        originalCalendar.getStartDate(),
        originalCalendar.getEndDate(),
        Visibility.PUBLIC,
        originalCalendar.getCategory(),
        color,
        originalCalendar.getId(),
        CalendarType.SCRAPED,
        originalCalendar.getPreviewStartDay(),
        originalCalendar.getPreviewEndDay()
    );
  }

  public static Calendar createOfficialCalendar(User user, String title, String description,
                                                LocalDate startDate, LocalDate endDate, CalendarColor color, Category category) {
    return createCalendarWithStartAndEndDay(
        user,
        title,
        description,
        startDate,
        endDate,
        Visibility.ADMIN,
        color,
        category,
        CalendarType.OFFICIAL);
  }

  // 배포 캘린더의 공개범위는 항상 public (공개범위 변경 불가)
  public static Calendar createDistributedCalendar(User user, Calendar officialCalendar) {

    return createCalendar(
        user
        , officialCalendar.getTitle()
        , officialCalendar.getDescription()
        , officialCalendar.getStartDate()
        , officialCalendar.getEndDate()
        , Visibility.PUBLIC
        , officialCalendar.getCategory()
        , officialCalendar.getColor()
        , officialCalendar.getId()
        , CalendarType.DISTRIBUTED
        , officialCalendar.getPreviewStartDay()
        , officialCalendar.getPreviewEndDay()
    );
  }

  // soft delete로 삭제
  public void softDelete() {
    this.deletedAt = Instant.now();
  }

  public void updateTitle(String title) {
    if (!isOriginalCalendar()) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "스크랩 캘린더는 제목을 변경할 수 없습니다.");
    }
    this.title = title;
  }

  public void updateDescription(String description) {
    if (!isOriginalCalendar()) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "스크랩 캘린더는 설명을 변경할 수 없습니다.");
    }
    this.description = description;
  }

  public void updateStartDate(LocalDate startDate) {
    if (!isOriginalCalendar()) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "스크랩 캘린더는 캘린더 시작일을 변경할 수 없습니다.");
    }
    this.startDate = startDate;
  }

  public void updateEndDate(LocalDate endDate) {
    if (!isOriginalCalendar()) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "스크랩 캘린더는 캘린더 종료일을 변경할 수 없습니다.");
    }
    this.endDate = endDate;
  }

  public void updateVisibility(Visibility visibility) {
    if (!isOriginalCalendar()) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "스크랩 캘린더는 공개범위를 변경할 수 없습니다.");
    }
    this.visibility = visibility;
  }

  public void updateCategory(Category category) {
    if (!isOriginalCalendar()) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "스크랩 캘린더는 카테고리를 변경할 수 없습니다.");
    }
    this.category = category;
  }

  public void updateColor(CalendarColor color) {
    this.color = color;
  }

  public void updateOriginalCalendarId(Long originalCalendarId) {
    if (isOriginalCalendar()) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "원본 캘린더는 originalCalendarId를 가질 수 없습니다.");
    }
    this.originalCalendarId = originalCalendarId;
  }

  public void updatePreviewStartDay(LocalDate previewStartDay) {
    if (!isOriginalCalendar()) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "스크랩 캘린더는 미리보기 시작일을 변경할 수 없습니다.");
    }
    this.previewStartDay = previewStartDay;
  }

  public void updatePreviewEndDay(LocalDate previewEndDay) {
    if (!isOriginalCalendar()) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "스크랩 캘린더는 미리보기 종료일을 변경할 수 없습니다.");
    }
    this.previewEndDay = previewEndDay;
  }

}
