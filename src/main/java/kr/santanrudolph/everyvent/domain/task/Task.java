package kr.santanrudolph.everyvent.domain.task;

import jakarta.persistence.*;
import kr.santanrudolph.everyvent.domain.calendar.entity.Calendar;
import kr.santanrudolph.everyvent.global.entity.BaseEntity;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

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

  private Task(Calendar calendar, String name, Instant startDate, Instant endDate) {
    this.calendar = calendar;
    this.name = name;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  public static Task create(Calendar calendar, String name, Instant startDate, Instant endDate) {

    if (startDate.isAfter(endDate)) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "종료일은 시작일과 같거나 이후의 날짜여야 합니다.");
    }
    return new Task(calendar, name, startDate, endDate);
  }

}