package io.bce.validation

import spock.lang.Specification
import spock.lang.Unroll

abstract class ValidationSpecification extends Specification {
	@Unroll
	def "Scenario: run validation case over object: #validationCase.getValidatableObject()"() {
		expect: "The validation case should be  ${validationCase.getExpectedRuleResult()}"
		validationCase.isPassed() == true
		
		and: "The validation state should contain validation errors: ${validationCase.getExpectedErrorMessages()}"
		validationCase.containsExpectedErrorMessages() == true
		
		1==1
		where:
		validationCase << getValidationCases();
	}
	
	protected abstract Collection<ValidationCase> getValidationCases();
}
