package io.bcs.common.domain.model.error

import io.bcs.common.domain.model.error.ApplicationException
import io.bcs.common.domain.model.error.ApplicationException.Severity
import spock.lang.Specification

class ApplicationExceptionSpec extends Specification {
	def "Scenario: error message generation"() {
		given: "The application exception"
		ApplicationException error = new DummyException(Severity.BUSINESS, "CONTEXT", 1L, "Some message");
		
		expect: "The message will have the next format: [BUSINESS] [CONTEXT__0001] Some message"
		error.message == "[BUSINESS] [CONTEXT__0001] Some message"
	}

	class DummyException extends ApplicationException {
		public DummyException(
		Severity severity,
		String context, Long errorNumber,
		String message) {
			super(severity, context, errorNumber, message);
		}
	}
}
