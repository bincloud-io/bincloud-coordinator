package io.bcs.common.errors;

import io.bce.domain.ErrorDescriptorTemplate;
import io.bce.domain.errors.ApplicationException;
import io.bce.domain.errors.ErrorDescriptor.ErrorSeverity;
import io.bce.domain.errors.UnexpectedErrorException;
import io.bce.domain.errors.ValidationException;
import io.bce.text.Text;
import io.bce.validation.ErrorMessage;
import io.bce.validation.ValidationState.ErrorState;
import io.bce.validation.ValidationState.GroupedError;
import io.bcs.fileserver.soap.types.common.FaultSeverityType;
import io.bcs.fileserver.soap.types.common.ServiceFaultType;
import io.bcs.fileserver.soap.types.common.ValidationErrorsType;
import io.bcs.fileserver.soap.types.common.ValidationGroupedErrorType;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.NonNull;

/**
 * This class represents the JAX-WS service response as a reaction on an occurred error.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class SoapFault extends ServiceFaultType {
  private SoapFault(Throwable throwable) {
    this(new UnexpectedErrorException(throwable));
  }

  private SoapFault(ApplicationException applicationError) {
    super();
    setBoundedContext(applicationError.getContextId().toString());
    setErrorNumber(applicationError.getErrorCode().extract());
    setSeverity(extractSeverity(applicationError));
    setMessage(Text.interpolate(ErrorDescriptorTemplate.createFor(applicationError)));
  }

  private SoapFault(ValidationException validationError) {
    this((ApplicationException) validationError);
    setValidationErrors(extractValidationErrors(validationError));
  }

  /**
   * Create the web service fault from the corresponding exception type.
   *
   * @param <E>   The exception type name
   * @param error The exception instance
   * @return The web service fault instance
   */
  public static <E extends Throwable> SoapFault createFor(@NonNull E error) {
    if (error instanceof ValidationException) {
      return new SoapFault((ValidationException) error);
    }

    if (error instanceof ApplicationException) {
      return new SoapFault((ApplicationException) error);
    }
    return new SoapFault(error);
  }

  private ValidationErrorsType extractValidationErrors(ValidationException validationError) {
    ValidationErrorsType result = new ValidationErrorsType();
    ErrorState errorState = validationError.getErrorState();
    result.getUngroupedErrors().addAll(interpolateErrorMessages(errorState.getUngroupedErrors()));
    result.getGroupedErrors().addAll(extractValidationGroupedErrors(errorState.getGroupedErrors()));
    return result;
  }

  private Collection<ValidationGroupedErrorType> extractValidationGroupedErrors(
      Collection<GroupedError> groupedErrors) {
    return groupedErrors.stream()
        .collect(Collectors.mapping(this::extractValidationGroupedError, Collectors.toList()));
  }

  private ValidationGroupedErrorType extractValidationGroupedError(GroupedError groupedError) {
    ValidationGroupedErrorType result = new ValidationGroupedErrorType();
    result.setGroupName(groupedError.getGroupName());
    result.getMessages().addAll(interpolateErrorMessages(groupedError.getMessages()));
    return result;
  }

  private Collection<String> interpolateErrorMessages(Collection<ErrorMessage> errorMessages) {
    return errorMessages.stream()
        .collect(Collectors.mapping(
            errorMessage -> Text.interpolate(new ErrorMessageTextTemplate(errorMessage)),
            Collectors.toList()));
  }

  private FaultSeverityType extractSeverity(ApplicationException applicationError) {
    if (applicationError.getErrorSeverity() == ErrorSeverity.INCIDENT) {
      return FaultSeverityType.INCIDENT;
    }
    return FaultSeverityType.BUSINESS;
  }
}
