package io.bce.domain.errors

import io.bce.domain.BoundedContextId
import io.bce.domain.errors.ErrorDescriptor.ErrorCode
import io.bce.domain.errors.ErrorDescriptor.ErrorSeverity
import spock.lang.Specification

class ApplicationExceptionSpec extends Specification {
  private static final BoundedContextId CONTEXT_ID = BoundedContextId.createFor("CONTEXT")
  private static final ErrorCode ERROR_CODE = ErrorCode.createFor(100L)

  def "Scenario: create error severity"() {
    given: "The simple application exception with predefined parameters"
    ApplicationException applicationException = new SimpleApplicationException(errorSeverity)

    expect: "The error predefined parameters should be accessible"
    applicationException.getContextId() == CONTEXT_ID
    applicationException.getErrorCode() == ERROR_CODE
    applicationException.getErrorSeverity() == errorSeverity

    and: "The error details should contain error message and stacktrace"
    Map<String, Object> errorDetails = applicationException.getErrorDetails()
    errorDetails.get(ApplicationException.ERROR_MESSAGE_DETAIL_NAME) == applicationException.getMessage()
    errorDetails.get(ApplicationException.ERROR_STACKTRACE_DETAIL_NAME) == new ErrorStackTrace(applicationException)

    where:
    errorSeverity << [
      ErrorSeverity.BUSINESS,
      ErrorSeverity.INCIDENT
    ]
  }

  private class SimpleApplicationException extends ApplicationException {
    public SimpleApplicationException(ErrorSeverity severity) {
      super(CONTEXT_ID, severity, ERROR_CODE)
    }
  }
}
