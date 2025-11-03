package kr.santanrudolph.everyvent.domain.calendar.dto.response;

import kr.santanrudolph.everyvent.domain.calendar.Category;
import kr.santanrudolph.everyvent.domain.calendar.Visibility;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Getter
@SuperBuilder
public abstract class CalendarResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Visibility visibility;
    private String color;
    private Category category;
    private Boolean isScrapable;

    protected static LocalDate toLocalDate(Instant instant, ZoneId userZone) {
        return instant != null ? instant.atZone(userZone).toLocalDate() : null;
    }
}
