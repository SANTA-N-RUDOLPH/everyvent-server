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
@DiscriminatorValue("ORIGINAL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OriginalCalendar extends Calendar {

    private Instant previewStartDate; // 공개할 task 시작일
    private Instant previewEndDate; // 공개할 task 종료일

    @Column(nullable = false)
    private boolean isOfficial; // 공식 캘린더 여부

    @Builder
    public OriginalCalendar(User user,
                            String title,
                            String description,
                            Instant startDate,
                            Instant endDate,
                            Visibility visibility,
                            String color,
                            Category category,
                            Instant previewStartDate,
                            Instant previewEndDate,
                            boolean isOfficial) {
        super(user, title, description, startDate, endDate, visibility, color, category);
        this.previewStartDate = previewStartDate;
        this.previewEndDate = previewEndDate;
        this.isOfficial = isOfficial;
    }

    @Override
    public boolean isScrapable() {
        return !isOfficial && getVisibility() != Visibility.PRIVATE;
    }

}