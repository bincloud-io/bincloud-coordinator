package io.bcs.port.adapters.common

import static io.bce.domain.errors.ErrorDescriptor.ErrorSeverity.BUSINESS

import io.bce.domain.BoundedContextId
import io.bce.domain.errors.ErrorDescriptor.ErrorCode
import io.bce.domain.errors.ErrorDescriptor.ErrorSeverity
import io.bce.domain.errors.ApplicationException
import io.bce.domain.errors.ValidationException
import io.bce.validation.ErrorMessage
import io.bce.validation.ValidationState
import io.bce.validation.ValidationState.ErrorState
import io.bcs.port.adapter.FaultSeverityType
import io.bcs.port.adapters.WSFault
import spock.lang.Specification

class WSFaultSpec extends Specification {
    private static final ErrorCode VALIDATION_ERROR_CODE = ErrorCode.createFor(1L)
    private static final ErrorCode APPLICATION_ERROR_CODE = ErrorCode.createFor(2L)

    def "Scenario: create for validation exception"() {
        expect: "The fault object, created for validation exception should contain not null validation errors"
        WSFault fault = WSFault.createFor(createValidationException())
        fault.getBoundedContext() == BoundedContextId.PLATFORM_CONTEXT.toString()
        fault.getErrorNumber() == 1L
        fault.getSeverity() == FaultSeverityType.BUSINESS
        fault.getMessage() == "ERROR.PLATFORM.1"
        fault.getValidationErrors().getUngroupedErrors().containsAll(["Something went wrong"])
    }

    def "Scenario: create for application exception"() {
        expect: "The fault object, created for application exception should not contain validation errors"
        WSFault fault = WSFault.createFor(new ApplicationException(BoundedContextId.PLATFORM_CONTEXT, BUSINESS, APPLICATION_ERROR_CODE))
        fault.getBoundedContext() == BoundedContextId.PLATFORM_CONTEXT.toString()
        fault.getErrorNumber() == 2L
        fault.getSeverity() == FaultSeverityType.BUSINESS
        fault.getMessage() == "ERROR.PLATFORM.2"
        fault.getValidationErrors() == null
    }
    
    def "Scenario: create for non-application exception"() {
        expect: "The fault object, created for non-application exception should be initialized as unrecognized incident"
        WSFault fault = WSFault.createFor(new RuntimeException())
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
