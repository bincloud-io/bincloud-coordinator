package io.bce.validation;

import io.bce.validation.ValidationContext.Validatable;
import io.bce.validation.ValidationExecutor.ValidationReport;
import java.util.Collection;

/**
 * This class executes validation case for grouped validations.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class GroupedValidationCase extends ValidationCase {
  private final String group;

  public GroupedValidationCase(Validatable validatable, String group, ExpectedResult expectedResult,
      Collection<String> expectedMessages) {
    super(validatable, expectedResult, expectedMessages);
    this.group = group;
  }

  @Override
  public boolean containsExpectedErrorMessages(ValidationReport validationReport) {
    return validationReport.containsGroupedErrors(group, getExpectedErrorMessages());
  }
}
