package kr.santanrudolph.everyvent.global.util;

import java.time.LocalDate;
import java.time.ZoneId;

public class TimeUtil {

  private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");

  public static LocalDate today() {
    return LocalDate.now(DEFAULT_ZONE);
  }

  public static boolean isAfter(LocalDate after, LocalDate before) {
    return after.isAfter(before);
  }

  public static boolean isInRange(LocalDate start, LocalDate end, LocalDate date) {
    return (date.isEqual(start) || date.isAfter(start)) &&
        (date.isEqual(end) || date.isBefore(end));
  }


  public static LocalDate today(ZoneId zoneId) {
    return LocalDate.now(zoneId);
  }

}

