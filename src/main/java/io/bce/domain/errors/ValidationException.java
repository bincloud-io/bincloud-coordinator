package io.bce.domain.errors;

import io.bce.domain.BoundedContextId;
import io.bce.validation.ErrorMessage;
import io.bce.validation.ValidationState;
import io.bce.validation.ValidationState.ErrorState;
import io.bce.validation.ValidationState.GroupedError;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 * This exception notifies about primary validation process fail.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@Getter
public class ValidationException extends ApplicationException {
  private static final long serialVersionUID = -2959637114905553972L;
  private static final String ERROR_MESSAGE = "Validation hasn't been passed with state: %s";
  public static final String ERROR_STATE_PROPERTY = "$$errorState";
  public static final String WRONG_PARAMETERS_PROPERTY = "$wrongParameters";
  public static final String UNGROUPED_ERRORS_PROPERTY = "$ungroupedErrors";
  public static final String GROUPED_ERRORS_PROPERTY = "$groupedErrors";

  private final ErrorState errorState;

  /**
   * Create the validation exception by a bounded context, error number and validation state.
   *
   * @param context         The bouned context id
   * @param errorNumber     The error number
   * @param validationState The validation state
   */
  public ValidationException(BoundedContextId context, ErrorCode errorNumber,
      ValidationState validationState) {
    super(context, ErrorSeverity.BUSINESS, errorNumber,
        String.format(ERROR_MESSAGE, validationState));
    this.errorState = validationState.getErrorState();
  }

  @Override
  public Map<String, Object> getErrorDetails() {
    Map<String, Object> details = new HashMap<String, Object>(super.getErrorDetails());
    Map<String, Collection<ErrorMessage>> groupedErrors = groupedErrorsMap();
    details.put(ERROR_STATE_PROPERTY, errorState);
    details.put(GROUPED_ERRORS_PROPERTY, groupedErrors);
    details.put(WRONG_PARAMETERS_PROPERTY, groupedErrors.keySet());
    details.put(UNGROUPED_ERRORS_PROPERTY, errorState.getUngroupedErrors());
    return details;
  }

  private Map<String, Collection<ErrorMessage>> groupedErrorsMap() {
    return errorState.getGroupedErrors().stream()
        .collect(Collectors.toMap(GroupedError::getGroupName, GroupedError::getMessages));
  }
}
