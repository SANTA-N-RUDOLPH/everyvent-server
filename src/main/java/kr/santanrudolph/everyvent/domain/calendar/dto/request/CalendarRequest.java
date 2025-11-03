package kr.santanrudolph.everyvent.domain.calendar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.santanrudolph.everyvent.domain.calendar.enums.Category;
import kr.santanrudolph.everyvent.domain.calendar.enums.Visibility;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CalendarRequest {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private Integer year;

    @NotNull
    private Integer month;

    @NotBlank
    private Visibility visibility;

    @NotBlank
    private String color;

    @NotBlank
    private Category category;
}
