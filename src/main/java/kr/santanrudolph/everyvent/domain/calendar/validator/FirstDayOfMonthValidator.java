package kr.santanrudolph.everyvent.domain.calendar.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class FirstDayOfMonthValidator implements ConstraintValidator<ValidFirstDayOfMonth, LocalDate> {

  @Override
  public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
    if (value == null) return true;
    return value.getDayOfMonth() == 1;
  }
}
