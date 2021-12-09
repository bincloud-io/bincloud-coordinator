package io.bce.validation;

import io.bce.validation.ValidationContext.Validatable;
import io.bce.validation.ValidationExecutor.ValidationReport;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.ToString.Include;

/**
 * This class is the base validation case check executor.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@ToString(onlyExplicitlyIncluded = true)
public abstract class ValidationCase {
  @Getter
  @Include
  private final Validatable validatableObject;
  private final ValidationExecutor validationExecutor;
  @Getter
  @Include
  private final ExpectedResult expectedRuleResult;
  @Getter
  @Include
  private final Collection<String> expectedErrorMessages;

  /**
   * Create validation case.
   *
   * @param validatable      The validatable value
   * @param expectedResult   The expected result type(PASSED or FAILED)
   * @param expectedMessages The expected error messages
   */
  public ValidationCase(Validatable validatable, ExpectedResult expectedResult,
      Collection<String> expectedMessages) {
    super();
    ValidationService validationService = DefaultValidationContext.createValidationService();
    this.validatableObject = validatable;
    this.validationExecutor = new ValidationExecutor(validatable, validationService);
    this.expectedErrorMessages = expectedMessages;
    this.expectedRuleResult = expectedResult;
  }

  /**
   * Execute validation case.
   *
   * @return The validation case report
   */
  public ValidationCaseReport execute() {
    ValidationReport report = validationExecutor.execute();
    return new ValidationCaseReport() {
      @Override
      public boolean isPassed() {
        return report.isPassed() == shouldBePassed();
      }

      @Override
      public boolean containsExpectedErrorMessages() {
        return ValidationCase.this.containsExpectedErrorMessages(report);
      }
    };
  }

  private boolean shouldBePassed() {
    return expectedRuleResult.shouldBeValid;
  }

  protected abstract boolean containsExpectedErrorMessages(ValidationReport validationReport);

  /**
   * The validation case report.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public interface ValidationCaseReport {
    /**
     * Check that the validation case is passed.
     *
     * @return True if passed and false otherwise
     */
    public boolean isPassed();

    /**
     * Check that all requested error messages are contained.
     *
     * @return True if contained and false otherwise
     */
    public boolean containsExpectedErrorMessages();
  }

  /**
   * This class represents expected result type.
   *
   * @author Dmiry Mikhaylenko
   *
   */
  @AllArgsConstructor
  public enum ExpectedResult {
    PASSED(true),
    FAILED(false);

    private boolean shouldBeValid;
  }
}
