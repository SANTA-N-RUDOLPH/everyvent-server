package kr.santanrudolph.everyvent.domain.calendar;


import kr.santanrudolph.everyvent.domain.calendar.enums.CalendarType;

import java.util.List;

public class CalendarConstants {
  public static final int DEFAULT_PERIOD_DAYS = 25;
  public static final int MAX_CNT_PER_MONTH = 3;

  // 월별 생성 제한 대상 캘린더 타입 (PERSONAL, SCRAPED만 제한)
  public static final List<CalendarType> LIMITED_CALENDAR_TYPES = List.of(
      CalendarType.PERSONAL,
      CalendarType.SCRAPED
  );

}

