package kr.santanrudolph.everyvent.domain.calendar.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CalendarColor {
  MINT(1, "민트", "#82DEAC"),
  BLUE(2, "블루", "#92A4FF"),
  LAVENDER(3, "라벤더", "#FBDFFF"),
  FUCHSIA_PINK(4, "푸시아 핑크", "#F0ABFC"),
  LIGHT_RED(5, "라이트 레드", "#DBE1FF"),
  PEACH(6, "피치", "#FFD6D7"),
  YELLOW(7, "옐로우", "#FFE5A0"),
  PURPLE(8, "퍼플", "#D8B4FE");

  private final int id;
  private final String label;
  private final String hexColor;

}
