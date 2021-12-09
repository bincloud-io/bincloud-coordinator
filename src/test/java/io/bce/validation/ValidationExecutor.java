package io.bce.validation;

import io.bce.validation.ValidationContext.Validatable;
import io.bce.validation.ValidationState.ErrorState;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

/**
 * This class executes validation over validatable objects.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class ValidationExecutor {
  private final Validatable validatable;
  private final ValidationService validationService;

  public ValidationReport execute() {
    return new ValidationReport(validationService.validate(validatable));
  }

  /**
   * This class contains information about executed validation.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  @RequiredArgsConstructor
  public static class ValidationReport {
    private final ValidationState validationState;

    /**
     * Check that the validation has been passed.
     *
     * @return True if passed and else otherwise
     */
    public boolean isPassed() {
      return validationState.isValid();
    }

    /**
     * Check that the validaion state contains specified ungrouped error messages.
     *
     * @param expectedMessages The expected error messages
     * @return True if validation state contains all error messages and false otherwise
     */
    public boolean containsUngroupedErrors(Collection<String> expectedMessages) {
      return stringifyErrorMessages(getUngroupedMessages()).containsAll(expectedMessages);
    }

    /**
     * Check that the validaion state contains specified grouped error messages.
     *
     * @param group            The requested validation group
     * @param expectedMessages The expected error messages
     * @return True if validation state contains all error messages and false otherwise
     */
    public boolean containsGroupedErrors(String group, Collection<String> expectedMessages) {
      return stringifyErrorMessages(getGroupedMessages(group)).containsAll(expectedMessages);
    }

    private ErrorState getErrorState() {
      return validationState.getErrorState();
    }

    private Collection<ErrorMessage> getUngroupedMessages() {
      return getErrorState().getUngroupedErrors();
    }

    private Collection<ErrorMessage> getGroupedMessages(String group) {
      return getErrorState().getGroupedErrors().stream()
          .filter(error -> error.getGroupName().equals(group))
          .flatMap(error -> error.getMessages().stream()).collect(Collectors.toList());
    }

    private Collection<String> stringifyErrorMessages(Collection<ErrorMessage> errorMessages) {
      return errorMessages.stream()
          .collect(Collectors.mapping(ErrorMessage::toString, Collectors.toList()));
    }
  }
}
