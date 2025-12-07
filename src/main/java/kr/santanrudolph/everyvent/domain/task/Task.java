package kr.santanrudolph.everyvent.domain.task;


import jakarta.persistence.*;
import kr.santanrudolph.everyvent.global.entity.BaseEntity;
import kr.santanrudolph.everyvent.domain.calendar.Calendar;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import kr.santanrudolph.everyvent.global.util.TimeUtil;
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

  @Column(nullable = false)
  private boolean canPreview;

  @Column(nullable = false)
  private boolean completed;

  public boolean getCompleted() {
    return completed;
  }

  public static Task createTask(Calendar calendar, LocalDate day, boolean canPreview) {
    Task task = new Task();
    task.calendar = calendar;
    task.day = day;
    task.canPreview = canPreview;
    task.completed = false;
    return task;
  }

  public void complete() {
    if (TimeUtil.isAfter(this.day, TimeUtil.today())) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "미래의 태스크는 완료할 수 없습니다.");
    }
    this.completed = true;
  }

  public void updateCanPreview(boolean canPreview) {
    this.canPreview = canPreview;
  }

}
