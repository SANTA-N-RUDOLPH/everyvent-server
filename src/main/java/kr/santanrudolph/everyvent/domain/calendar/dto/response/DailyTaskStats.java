package kr.santanrudolph.everyvent.domain.calendar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DailyTaskStats {
  private int totalCount;
  private int completedCount;
  private int pendingCount;
}
