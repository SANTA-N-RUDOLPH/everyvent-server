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
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "tasks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Task extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "calendar_id", nullable = false)
  private Calendar calendar;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private Instant startDate;

  @Column(nullable = false)
  private Instant endDate;

  @Column(columnDefinition = "jsonb")
  @Convert(converter = DailyStatusConverter.class)
  private Map<String, Boolean> dailyStatus;

  public void markCompleted(Instant date) {
    String key = validateTaskPeriod(date);
    dailyStatus.put(key, true);
  }

  public void unmarkCompleted(Instant date) {
    String key = validateTaskPeriod(date);
    dailyStatus.put(key, false);
  }

  public boolean isCompleted(Instant date) {
    String key = validateTaskPeriod(date);
    return dailyStatus.get(key);
  }

  private Task(Calendar calendar, String name, Instant startDate, Instant endDate) {
    validateName(name);
    validateDate(startDate, endDate);

    this.calendar = calendar;
    this.name = name;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  public static Task create(Calendar calendar, String name, Instant startDate, Instant endDate) {
    Task task = new Task(calendar, name, startDate, endDate);
    task.dailyStatus = task.generateDailyStatus(startDate, endDate);
    return task;
  }

  private Map<String, Boolean> generateDailyStatus(Instant start, Instant end) {
    Map<String, Boolean> status = new HashMap<>();

    Instant current = start.truncatedTo(ChronoUnit.DAYS);
    Instant endDay = end.truncatedTo(ChronoUnit.DAYS);

    while (!current.isAfter(endDay)) {
      status.put(current.toString(), false);
      current = current.plus(1, ChronoUnit.DAYS);
    }

    return status;
  }

  // 필드 업데이트 메서드
  public void updateName(String name) {
    validateName(name);
    this.name = name;
  }

  public void updatePeriod(Instant startDate, Instant endDate) {
    validateDate(startDate, endDate);
    this.startDate = startDate;
    this.endDate = endDate;
    this.dailyStatus = generateDailyStatus(startDate, endDate);
  }

  // 검증 메서드
  private String validateTaskPeriod(Instant date) {
    String key = date.truncatedTo(ChronoUnit.DAYS).toString();
    if (!dailyStatus.containsKey(key)) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "해당 날짜는 태스크 기간에 포함되지 않습니다.");
    }
    return key;
  }

  private static void validateName(String name) {
    if (name == null || name.isBlank()) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "태스크 이름은 필수입니다.");
    }
  }

  private static void validateDate(Instant startDate, Instant endDate) {
    if (startDate.isAfter(endDate)) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "종료일은 시작일과 같거나 이후의 날짜여야 합니다.");
    }
  }

}