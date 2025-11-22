package kr.santanrudolph.everyvent.domain.task;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
  name = "daily_tasks",
  indexes = {
    @Index(name = "idx_task_date", columnList = "task_id, date"),
    @Index(name = "idx_date_isCompleted", columnList = "date, isCompleted")
  }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyTask {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "task_id", nullable = false)
  private Task task;

  @Column(nullable = false)
  private Instant date;

  @Column(nullable = false)
  private boolean isCompleted;

  private DailyTask(Task task, Instant date) {
    this.task = task;
    this.date = date;
    this.isCompleted = false;
  }

  public static DailyTask create(Task task, Instant date) {
    return new DailyTask(task, date);
  }

  public void markCompleted() {
    this.isCompleted = true;
  }

  public void unmarkCompleted() {
    this.isCompleted = false;
  }

}
