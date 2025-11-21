package kr.santanrudolph.everyvent.domain.task;

import jakarta.persistence.*;
import kr.santanrudolph.everyvent.global.entity.BaseEntity;
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

  @Builder
  public DailyTask(Task task, Instant date) {
    this.task = task;
    this.date = date;
    this.isCompleted = false;
  }

}