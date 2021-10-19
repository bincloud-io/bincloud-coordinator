package io.bcs.common.domain.model.error

import io.bcs.common.domain.model.error.UnexpectedSystemBehaviorException
import io.bcs.common.domain.model.error.ApplicationException.Severity
import spock.lang.Specification

class UnexpectedSystemBehaviorExceptionSpec extends Specification {
	def "Scenario: create exception by constructor for existing exception"() {
		given: "The exception"
		Exception error = new Exception("Something went wrong")
		
		when: "The exception has been wrapped"
		UnexpectedSystemBehaviorException wrappedError = new UnexpectedSystemBehaviorException("ERR_CTX", error)
		
		then: "Their messages should be the same"
		wrappedError.getMessage() == error.getMessage()
		
		and: "The original exception should be cause of wrapping exception"
		wrappedError.getCause().is(error)
		
		and: "The context should be the same as passed to constructor"
		wrappedError.getContext() == "ERR_CTX"
		
		and: "The system error code should be -1"
		wrappedError.getErrorCode() == -1L
		
		and: "The wrapped error should be the incident"
		wrappedError.getSeverity() == Severity.INCIDENT
	}
}
