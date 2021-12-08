package io.bce.domain.errors

import io.bce.domain.BoundedContextId
import io.bce.domain.errors.ErrorDescriptor.ErrorCode
import io.bce.domain.errors.ErrorDescriptor.ErrorSeverity
import spock.lang.Specification

class UnexpectedErrorExceptionSpec extends Specification {
  private static final BoundedContextId CONTEXT_ID = BoundedContextId.createFor("CONTEXT")
  private static final BoundedContextId ANOTHER_CONTEXT_ID = BoundedContextId.createFor("ANOTHER_CONTEXT")
  private static final ErrorCode ERROR_CODE = ErrorCode.createFor(100L)
  private static final Exception NON_APPLICATION_ERROR = new Exception("ERROR")
  private static final Exception APPLICATION_ERROR = new SimpleApplicationException();


  def "Scenario: wrap error object for specified bounded context"() {
    given: "The wrapped exception with specified context"
    UnexpectedErrorException wrappedError = new UnexpectedErrorException(ANOTHER_CONTEXT_ID, unexpectedError)

    expect: "The bounded context should be predefined independently of error type"
    wrappedError.getContextId() == expectedBoundedContext

    and: "The error code should be initialized by ${ErrorCode.UNRECOGNIZED_ERROR_CODE}"
    wrappedError.getErrorCode() == ErrorCode.UNRECOGNIZED_ERROR_CODE

    and: "The error severity should be incident"
    wrappedError.getErrorSeverity() == ErrorSeverity.INCIDENT

    where:
    unexpectedError       | expectedBoundedContext
    APPLICATION_ERROR     | ANOTHER_CONTEXT_ID
    NON_APPLICATION_ERROR | ANOTHER_CONTEXT_ID
  }

  def "Scenario: wrap error object for unknown bounded context"() {
    given: "The wrapped exception with unspecified context"
    UnexpectedErrorException wrappedError = new UnexpectedErrorException(unexpectedError)

    expect: "The bounded context should be predefined independently of error type"
    wrappedError.getContextId() == expectedBoundedContext

    and: "The error code should be initialized by ${ErrorCode.UNRECOGNIZED_ERROR_CODE}"
    wrappedError.getErrorCode() == ErrorCode.UNRECOGNIZED_ERROR_CODE

    and: "The error severity should be incident"
    wrappedError.getErrorSeverity() == ErrorSeverity.INCIDENT

    where:
    unexpectedError       | expectedBoundedContext
    APPLICATION_ERROR     | CONTEXT_ID
    NON_APPLICATION_ERROR | BoundedContextId.PLATFORM_CONTEXT
  }

  private static class SimpleApplicationException extends ApplicationException {
    public SimpleApplicationException() {
      super(CONTEXT_ID, ErrorSeverity.BUSINESS, ERROR_CODE)
    }
  }
}
