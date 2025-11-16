package kr.santanrudolph.everyvent.domain.calendar;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class InstantRange {

    private static final int CALENDAR_START_DAY = 1;
    private static final int CALENDAR_END_DAY = 25;

    private final Instant start;
    private final Instant end;

    private InstantRange(Instant start, Instant end) {
        this.start = start;
        this.end = end;
    }

    public static InstantRange of(int year, int month) {

        LocalDate startLocal = LocalDate.of(year, month, CALENDAR_START_DAY);
        LocalDate endLocal = LocalDate.of(year, month, CALENDAR_END_DAY);

        ZoneId koreaZone = ZoneId.of("Asia/Seoul");

        return new InstantRange(
                toStartOfDay(startLocal, koreaZone),
                toStartOfDay(endLocal, koreaZone)
        );
    }

    public Instant start() { return start; }
    public Instant end() { return end; }

    private static Instant toStartOfDay(LocalDate date, ZoneId zone) {
        return date.atStartOfDay(zone).toInstant();
    }

}