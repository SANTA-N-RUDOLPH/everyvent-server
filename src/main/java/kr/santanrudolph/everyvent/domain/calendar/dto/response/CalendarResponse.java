package kr.santanrudolph.everyvent.domain.calendar.dto.response;

import kr.santanrudolph.everyvent.domain.calendar.entity.Calendar;
import kr.santanrudolph.everyvent.domain.calendar.enums.Category;
import kr.santanrudolph.everyvent.domain.calendar.enums.Visibility;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter
@SuperBuilder
public abstract class CalendarResponse {

    private Long id;
    private String title;
    private String description;
    private Instant startDate;
    private Instant endDate;
    private Visibility visibility;
    private String color;
    private Category category;
    private Boolean isScrapable;

    @SuppressWarnings("unchecked")
    protected static <B extends CalendarResponseBuilder<?, ?>> B fillCommonFields(
            B builder, Calendar calendar, boolean isScrapable) {

        return (B) builder
                .id(calendar.getId())
                .title(calendar.getTitle())
                .description(calendar.getDescription() != null ? calendar.getDescription() : "")
                .startDate(calendar.getStartDate())
                .endDate(calendar.getEndDate())
                .visibility(calendar.getVisibility())
                .color(calendar.getColor())
                .category(calendar.getCategory())
                .isScrapable(isScrapable);
    }

}