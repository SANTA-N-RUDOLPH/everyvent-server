package kr.santanrudolph.everyvent.domain.calendar.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Constraint(validatedBy = HexColorValidator.class)
@Target({ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidColor {
  String message() default "유효한 HEX 색상 코드여야 합니다 (예: #FFF, #FFFFFF)";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}
