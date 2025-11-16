package kr.santanrudolph.everyvent.domain.calendar.entity;

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
                          Instant previewEndDate) {
    super(user, title, description, startDate, endDate, visibility, color, category);
    updatePreviewPeriod(previewStartDate, previewEndDate);
  }

  @Override
  public boolean isScrapable() {
    return getVisibility() != Visibility.PRIVATE;
  }

  public void updatePreviewPeriod(Instant previewStartDate, Instant previewEndDate) {
    if (previewStartDate == null || previewEndDate == null) {
      this.previewStartDate = null;
      this.previewEndDate = null;
      return;
    }

    this.previewStartDate = previewStartDate;
    this.previewEndDate = previewEndDate;
  }

}