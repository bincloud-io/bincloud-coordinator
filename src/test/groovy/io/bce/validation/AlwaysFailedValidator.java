package io.bce.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class AlwaysFailedValidator implements ConstraintValidator<AlwaysFailed, Object> {
  @Override
  public void initialize(AlwaysFailed constraintAnnotation) {
  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    return false;
  }
}
