package kr.santanrudolph.everyvent.domain.task;

import jakarta.persistence.*;
import kr.santanrudolph.everyvent.global.entity.BaseEntity;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "daily_tasks", uniqueConstraints = @UniqueConstraint(columnNames = {"task_id", "date"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyTask extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "task_id", nullable = false)
  private Task task;

  @Column(nullable = false)
  private Instant date;

  @Column(nullable = false)
  private Boolean isCompleted;

  private DailyTask(Task task, Instant date) {
    this.task = task;
    this.date = date;
    this.isCompleted = false;
  }

  public static DailyTask create(Task task, Instant date) {

    if (date.isBefore(task.getStartDate()) || date.isAfter(task.getEndDate())) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "DailyTask의 날짜는 Task 기간 내에 있어야 합니다.");
    }
    return new DailyTask(task, date);
  }

}