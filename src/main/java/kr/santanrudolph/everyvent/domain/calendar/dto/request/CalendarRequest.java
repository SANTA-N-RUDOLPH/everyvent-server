package kr.santanrudolph.everyvent.domain.calendar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private String visibility;

    @NotBlank
    private String color;

    @NotBlank
    private String category;
}
