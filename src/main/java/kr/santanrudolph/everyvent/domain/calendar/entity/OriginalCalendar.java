package kr.santanrudolph.everyvent.domain.calendar.entity;

import jakarta.persistence.*;
import kr.santanrudolph.everyvent.domain.calendar.enums.Category;
import kr.santanrudolph.everyvent.domain.calendar.enums.Visibility;
import kr.santanrudolph.everyvent.domain.user.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@DiscriminatorValue("ORIGINAL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OriginalCalendar extends Calendar {

  private LocalDate previewStartDate; // 공개할 task 시작일
  private LocalDate previewEndDate; // 공개할 task 종료일

  @Builder
  public OriginalCalendar(User user,
                          String title,
                          String description,
                          LocalDate startDate,
                          LocalDate endDate,
                          LocalDate previewStartDate,
                          LocalDate previewEndDate,
                          Visibility visibility,
                          String color,
                          Category category) {
    super(user, title, description, startDate, endDate, visibility, color, category);
    updatePreviewPeriod(previewStartDate, previewEndDate);
  }

  @Override
  public boolean isScrapable() {
    return getVisibility() != Visibility.PRIVATE;
  }

  public void updatePreviewPeriod(LocalDate previewStartDate, LocalDate previewEndDate) {
    if (previewStartDate == null || previewEndDate == null) {
      this.previewStartDate = null;
      this.previewEndDate = null;
      return;
    }

    this.previewStartDate = previewStartDate;
    this.previewEndDate = previewEndDate;
  }

}
