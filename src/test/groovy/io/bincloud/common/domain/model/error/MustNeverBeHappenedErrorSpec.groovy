package io.bincloud.common.domain.model.error

import spock.lang.Specification

class MustNeverBeHappenedErrorSpec extends Specification {
	def "Scenraio: initialize error from exception"() {
		given: "The exception to initialization"
		Exception exception = new Exception("Something went wrong")

		when: "The must never be happened error has been initialized"
		MustNeverBeHappenedError error = new MustNeverBeHappenedError(exception)

		then: "The message should be \"Error java.lang.Exception must never be happened for this case.\""
		error.getMessage() == "Error class java.lang.Exception must never be happened for this case."

		and: "The exception should be cause of error"
		error.getCause() == exception
	}

	def "Scenario: initialize error from message text"() {
		when: "The must never be happened error has been initialized by message"
		MustNeverBeHappenedError error = new MustNeverBeHappenedError("Something went wrong")

		then: "The message should be the same as in the initial exception"
		error.getMessage() == "Something went wrong"
	}
}
