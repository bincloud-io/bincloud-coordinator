package io.bce.validation;

import java.util.Collection;

import io.bce.validation.ValidationContext.Validatable;
import io.bce.validation.ValidationExecutor.ValidationReport;

public class UngroupedValidationCase extends ValidationCase {
  public UngroupedValidationCase(Validatable validatable, ExpectedResult expectedResult,
      Collection<String> expectedMessages) {
    super(validatable, expectedResult, expectedMessages);
  }

  @Override
  protected boolean containsExpectedErrorMessages(ValidationReport validationReport) {
    return validationReport.containsUngroupedErrors(getExpectedErrorMessages());
  }
}
