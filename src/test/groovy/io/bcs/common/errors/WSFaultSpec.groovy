package io.bcs.common.errors

import static io.bce.domain.errors.ErrorDescriptor.ErrorSeverity.BUSINESS

import io.bce.domain.BoundedContextId
import io.bce.domain.errors.ApplicationException
import io.bce.domain.errors.ValidationException
import io.bce.domain.errors.ErrorDescriptor.ErrorCode
import io.bce.validation.ErrorMessage
import io.bce.validation.ValidationState
import io.bcs.fileserver.soap.types.common.FaultSeverityType
import spock.lang.Specification

class WSFaultSpec extends Specification {
  private static final ErrorCode VALIDATION_ERROR_CODE = ErrorCode.createFor(1L)
  private static final ErrorCode APPLICATION_ERROR_CODE = ErrorCode.createFor(2L)

  def "Scenario: create for validation exception"() {
    expect: "The fault object, created for validation exception should contain not null validation errors"
    SoapFault fault = SoapFault.createFor(createValidationException())
    fault.getBoundedContext() == BoundedContextId.PLATFORM_CONTEXT.toString()
    fault.getErrorNumber() == 1L
    fault.getSeverity() == FaultSeverityType.BUSINESS
    fault.getMessage() == "ERROR.PLATFORM.1"
    fault.getValidationErrors().getUngroupedErrors().containsAll(["Something went wrong"])
  }

  def "Scenario: create for application exception"() {
    expect: "The fault object, created for application exception should not contain validation errors"
    SoapFault fault = SoapFault.createFor(new ApplicationException(BoundedContextId.PLATFORM_CONTEXT, BUSINESS, APPLICATION_ERROR_CODE))
    fault.getBoundedContext() == BoundedContextId.PLATFORM_CONTEXT.toString()
    fault.getErrorNumber() == 2L
    fault.getSeverity() == FaultSeverityType.BUSINESS
    fault.getMessage() == "ERROR.PLATFORM.2"
    fault.getValidationErrors() == null
  }

  def "Scenario: create for non-application exception"() {
    expect: "The fault object, created for non-application exception should be initialized as unrecognized incident"
    SoapFault fault = SoapFault.createFor(new RuntimeException())
    fault.getBoundedContext() == BoundedContextId.PLATFORM_CONTEXT.toString()
    fault.getErrorNumber() == -1L
    fault.getSeverity() == FaultSeverityType.INCIDENT
    fault.getMessage() == "ERROR.PLATFORM.-1"
    fault.getValidationErrors() == null
  }


  private ValidationException createValidationException() {
    return new ValidationException(
        BoundedContextId.PLATFORM_CONTEXT, VALIDATION_ERROR_CODE,
        new ValidationState().withUngrouped(ErrorMessage.createFor("Something went wrong")))
  }
}
