package kr.santanrudolph.everyvent.domain.calendar;

import jakarta.persistence.*;
import kr.santanrudolph.everyvent.domain.user.Role;
import kr.santanrudolph.everyvent.domain.user.User;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
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
    public OriginalCalendar(User user, String title, String description, Instant startDate, Instant endDate, Visibility visibility, String color, Category category,
                            Instant previewStartDate, Instant previewEndDate, boolean isOfficial) {
        super(user, title, description, startDate, endDate, visibility, color, category);
        this.previewStartDate = previewStartDate;
        this.previewEndDate = previewEndDate;
        this.isOfficial = isOfficial;
    }

    @Override
    public boolean isScrapable() {
        return !isOfficial && getVisibility() != Visibility.PRIVATE;
    }

    @Override
    public void changeVisibility(Visibility newVisibility) {
        super.changeVisibility(newVisibility);
        updatePreviewAvailability();
    }

    // === 공식 캘린더 관련 메서드 ===
    public void changeOfficialStatus(boolean isOfficial) {
        this.isOfficial = isOfficial;
        updatePreviewAvailability();
    }

    // === 미리보기 관련 메서드 ===
    public void setPreviewPeriod(Instant previewStartDate, Instant previewEndDate) {
        if (isOfficial || getVisibility() == Visibility.PRIVATE) {
            updatePreviewAvailability();
            return;
        }
        validateDateRange(previewStartDate, previewEndDate);
        validateWithinCalendarPeriod(previewStartDate, previewEndDate);

        this.previewStartDate = previewStartDate;
        this.previewEndDate = previewEndDate;
    }

    public boolean hasPreviewPeriod() {
        return previewStartDate != null && previewEndDate != null;
    }

    public void updatePreviewAvailability() {
        if (isOfficial || getVisibility() == Visibility.PRIVATE) {
            this.previewStartDate = null;
            this.previewEndDate = null;
        }
    }

    public boolean isTaskPreviewable(Instant taskDate) {
        if (!hasPreviewPeriod()) {
            return false;
        }
        return !taskDate.isBefore(previewStartDate) && !taskDate.isAfter(previewEndDate);
    }

    // === 검증 관련 메서드 ===
    private void validateWithinCalendarPeriod(Instant start, Instant end) {
        if (start.isBefore(getStartDate()) || end.isAfter(getEndDate())) {
            throw new IllegalArgumentException("미리보기 기간은 캘린더 기간 내에 있어야 합니다");
        }
    }

    public void ensureOfficialEditableByUser() {
        if (this.isOfficial() && this.getUser().getRole() == Role.USER) {
            throw new EveryventException(ErrorCode.OFFICIAL_CALENDAR_CANNOT_MODIFY);
        }
    }
}