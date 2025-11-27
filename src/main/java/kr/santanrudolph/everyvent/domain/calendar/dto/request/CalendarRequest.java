package kr.santanrudolph.everyvent.domain.calendar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kr.santanrudolph.everyvent.domain.calendar.enums.Category;
import kr.santanrudolph.everyvent.domain.calendar.enums.Visibility;
import kr.santanrudolph.everyvent.domain.calendar.validator.ValidColor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class CalendarRequest {

  @NotBlank
  @Size(max = 20, message = "제목은 최대 20자까지 입력할 수 있습니다.")
  private String title;

  @Size(max = 150, message = "설명은 최대 150자까지 입력할 수 있습니다.")
  private String description;

  @NotNull
  private LocalDate startDate;

  @NotNull
  private Visibility visibility;

  @NotBlank
  @ValidColor
  private String color;

  @NotNull
  private Category category;
}
