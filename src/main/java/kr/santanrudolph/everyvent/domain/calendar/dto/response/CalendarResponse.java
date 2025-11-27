package kr.santanrudolph.everyvent.domain.calendar.dto.response;

import kr.santanrudolph.everyvent.domain.calendar.entity.Calendar;
import kr.santanrudolph.everyvent.domain.calendar.entity.DistributedCalendar;
import kr.santanrudolph.everyvent.domain.calendar.entity.OfficialCalendar;
import kr.santanrudolph.everyvent.domain.calendar.entity.OriginalCalendar;
import kr.santanrudolph.everyvent.domain.calendar.enums.Category;
import kr.santanrudolph.everyvent.domain.calendar.enums.Visibility;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import lombok.Getter;

import java.time.LocalDate;

@Getter
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

    protected CalendarResponse(Long id,
                               String title,
                               String description,
                               LocalDate startDate,
                               LocalDate endDate,
                               Visibility visibility,
                               String color,
                               Category category,
                               Boolean isScrapable) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.visibility = visibility;
        this.color = color;
        this.category = category;
        this.isScrapable = isScrapable;
    }

    public static CalendarResponse from(
            Calendar calendar,
            OfficialCalendar official,
            boolean isScrapable
    ) {
        if (calendar instanceof OriginalCalendar originalCalendar) {
            if (official != null) {
                return OfficialCalendarResponse.from(official);
            }
            return OriginalCalendarResponse.from(originalCalendar, isScrapable);

        } else if (calendar instanceof DistributedCalendar distributedCalendar) {
            return DistributedCalendarResponse.from(distributedCalendar);
        }

        throw new EveryventException(ErrorCode.INVALID_INPUT, "지원하지 않는 캘린더 타입입니다.");
    }
}
