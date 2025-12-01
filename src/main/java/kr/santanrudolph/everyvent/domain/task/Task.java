package kr.santanrudolph.everyvent.domain.task;

import jakarta.persistence.*;
import kr.santanrudolph.everyvent.domain.calendar.entity.Calendar;
import kr.santanrudolph.everyvent.global.entity.BaseEntity;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "tasks",
        indexes = {
                @Index(name = "idx_calendar_day", columnList = "calendar_id, day_of_month")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_task_calendar_day_name",
                        columnNames = {"calendar_id", "day_of_month", "name"}
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

  @Column(name = "day_of_month", nullable = false)
  private int day;

  @Column(nullable = false)
  private boolean isCompleted = false;

  private Task(Calendar calendar, String name, int day) {
    if (calendar == null) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "캘린더는 필수입니다.");
    }
    validateName(name);
    validateDay(day, calendar);

    this.calendar = calendar;
    this.name = name;
    this.day = day;
  }

  public static Task create(Calendar calendar, String name, int day) {
    return new Task(calendar, name, day);
  }

  // 필드 업데이트 메서드
  public void updateName(String name) {
    validateName(name);
    this.name = name;
  }

  public void updateDay(int day) {
    validateDay(day, this.calendar);
    this.day = day;
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

  private static void validateDay(int day, Calendar calendar) {
    int startDay = calendar.getStartDate().getDayOfMonth();
    int endDay = calendar.getEndDate().getDayOfMonth();

    if (day < startDay || day > endDay) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "캘린더 기간 내의 날짜이어야 합니다.");
    }
  }

}
