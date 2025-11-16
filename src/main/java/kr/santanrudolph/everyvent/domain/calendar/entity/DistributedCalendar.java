package kr.santanrudolph.everyvent.domain.calendar.entity;

import jakarta.persistence.*;
import kr.santanrudolph.everyvent.domain.user.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("DISTRIBUTED")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DistributedCalendar extends Calendar {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_calendar_id")
    private OriginalCalendar originalCalendar;

    @Builder
    public DistributedCalendar(
            User user,
            OriginalCalendar calendar
    ) {
      super(user,
            calendar.getTitle(),
            calendar.getDescription(),
            calendar.getStartDate(),
            calendar.getEndDate(),
            calendar.getVisibility(),
            calendar.getColor(),
            calendar.getCategory());
      this.originalCalendar = calendar;
    }

  public static DistributedCalendar of(User user, OriginalCalendar originalCalendar) {
    return new DistributedCalendar(user, originalCalendar);
  }

  @Override
  public boolean isScrapable() {
    return false;
  }

}
