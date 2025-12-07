package kr.santanrudolph.everyvent.domain.calendar.dto;

import jakarta.validation.constraints.Size;
import kr.santanrudolph.everyvent.domain.calendar.enums.CalendarColor;
import kr.santanrudolph.everyvent.domain.calendar.enums.Category;
import kr.santanrudolph.everyvent.domain.calendar.enums.Visibility;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class CalendarUpdateRequest {

  @Size(min = 1, max = 50, message = "캘린더 제목은 1~50자 사이여야 합니다.")
  private String title;

  @Size(max = 500, message = "설명은 최대 500자까지 입력 가능합니다.")
  private String description;

  private Visibility visibility;

  private CalendarColor color;

  private Category category;

}
