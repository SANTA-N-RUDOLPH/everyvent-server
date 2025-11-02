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

    private Long officialId; // 복제된 공식 캘린더의 원본 id

    @Builder
    public OriginalCalendar(User user, String title, String description, Instant startDate, Instant endDate, Visibility visibility, String color, Category category,
                            Instant previewStartDate, Instant previewEndDate, boolean isOfficial, Long officialId) {
        super(user, title, description, startDate, endDate, visibility, color, category);
        setPreviewPeriod(previewStartDate, previewEndDate);
        this.isOfficial = isOfficial;
        this.officialId = officialId;
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

    public void enterOfficialId(Long officialId) {
        this.officialId = officialId;
    }

    // === 미리보기 관련 메서드 ===
    public void setPreviewPeriod(Instant previewStartDate, Instant previewEndDate) {
        if (previewStartDate == null || previewEndDate == null) {
            this.previewStartDate = null;
            this.previewEndDate = null;
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
            throw new EveryventException(ErrorCode.PREVIEW_PERIOD_OUT_OF_CALENDAR);
        }
    }

    public void ensureOfficialEditableByUser() {
        if (this.officialId != null && this.getUser().getRole() == Role.USER) {
            throw new EveryventException(ErrorCode.OFFICIAL_CALENDAR_CANNOT_MODIFY);
        }
    }
}