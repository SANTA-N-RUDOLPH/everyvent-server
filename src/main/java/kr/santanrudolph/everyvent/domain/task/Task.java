package kr.santanrudolph.everyvent.domain.task;

import jakarta.persistence.*;
import kr.santanrudolph.everyvent.domain.calendar.entity.Calendar;
import kr.santanrudolph.everyvent.global.entity.BaseEntity;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
  name = "tasks",
  indexes = {
          @Index(name = "idx_calendar_date", columnList = "calendar_id, date")
  },
  uniqueConstraints = {
          @UniqueConstraint(
                  name = "uk_task_calendar_date_name",
                  columnNames = {"calendar_id", "date", "name"}
          )
  }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Task extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "calendar_id", nullable = false)
  private Calendar calendar;

  @Column(nullable = false, length = 30)
  private String name;

  @Column(nullable = false)
  private Instant date;

  @Column(nullable = false)
  private boolean isCompleted;

  private Task(Calendar calendar, String name, Instant date) {
    if (calendar == null) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "캘린더는 필수입니다.");
    }
    validateName(name);
    validateDate(date, calendar);

    this.calendar = calendar;
    this.name = name;
    this.date = date;
    this.isCompleted = false;
  }

  public static Task create(Calendar calendar, String name, Instant date) {
    return new Task(calendar, name, date);
  }

  // 필드 업데이트 메서드
  public void updateName(String name) {
    validateName(name);
    this.name = name;
  }

  public void updateDate(Instant date) {
    validateDate(date, this.calendar);
    this.date = date;
  }

  public void markCompleted() {
    this.isCompleted = true;
  }

  public void unmarkCompleted() {
    this.isCompleted = false;
  }

  // 검증 메서드
  private static void validateName(String name) {
    if (name == null || name.isBlank()) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "태스크 이름은 필수입니다.");
    }
    if (name.length() > 30) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "태스크 이름은 30자를 초과할 수 없습니다.");
    }
  }

  private static void validateDate(Instant date, Calendar calendar) {
    if (date == null) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "날짜는 필수입니다.");
    }
    if (date.isBefore(calendar.getStartDate()) || date.isAfter(calendar.getEndDate())) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "캘린더 범위 내 날짜여야 합니다.");
    }
  }

}