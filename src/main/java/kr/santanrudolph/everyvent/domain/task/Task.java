package kr.santanrudolph.everyvent.domain.task;


import jakarta.persistence.*;
import kr.santanrudolph.everyvent.global.entity.BaseEntity;
import kr.santanrudolph.everyvent.domain.calendar.Calendar;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
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
  private LocalDate day;

  @Column(nullable = false)
  private String content;

  @Column
  private Boolean completed;

  public static Task createTask(Calendar calendar, LocalDate day, String content) {
    Task task = new Task();
    task.calendar = calendar;
    task.day = day;
    task.content = content;
    task.completed = false;
    return task;
  }

  public void updateContent(String content) {
    this.content = content;
  }

  public void complete() {
    this.completed = true;
  }

  public void uncomplete() {
    this.completed = false;
  }
}
