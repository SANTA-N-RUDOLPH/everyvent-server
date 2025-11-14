package kr.santanrudolph.everyvent.domain.calendar;

import jakarta.persistence.*;
import kr.santanrudolph.everyvent.domain.calendar.enums.Category;
import kr.santanrudolph.everyvent.domain.calendar.enums.Visibility;
import kr.santanrudolph.everyvent.domain.user.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@DiscriminatorValue("DISTRIBUTED")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DistributedCalendar extends Calendar{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_calendar_id", nullable = false)
    private OriginalCalendar originalCalendar;

    @Column(nullable = false)
    private Instant distributedAt;

    @Builder
    public DistributedCalendar(
            User user,
            OriginalCalendar originalCalendar,
            String title,
            String description,
            Instant startDate,
            Instant endDate,
            Visibility visibility,
            String color,
            Category category
    ) {
        super(user, title, description, startDate, endDate, visibility, color, category);
        this.originalCalendar = originalCalendar;
        this.distributedAt = Instant.now();
    }

    @Override
    public boolean isScrapable() {
        return false;
    }

}
