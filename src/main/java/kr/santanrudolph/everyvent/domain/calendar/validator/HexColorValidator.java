package kr.santanrudolph.everyvent.domain.calendar.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class HexColorValidator implements ConstraintValidator<ValidColor, String> {

  private static final String HEX_COLOR_PATTERN = "^#([0-9A-Fa-f]{3}|[0-9A-Fa-f]{6})$";

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
      return value != null && value.matches(HEX_COLOR_PATTERN);
  }
}
