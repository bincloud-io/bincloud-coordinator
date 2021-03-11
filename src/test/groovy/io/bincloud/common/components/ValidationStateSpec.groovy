package io.bincloud.common.components

import io.bincloud.common.ApplicationException.Severity
import io.bincloud.common.validation.ValidationException
import io.bincloud.common.validation.ValidationState
import io.bincloud.common.validation.ValidationState.ErrorState
import spock.lang.Specification

class ValidationStateSpec extends Specification {
	def "Scenario: create empty validation state"() {
		given: "The empty validation state"
		ValidationState validationState = new ValidationState()

		expect: "The error state is valid"
		ErrorState errorState = validationState.getErrorState()
		validationState.valid == true

		and: "The grouped errors in error state are empty"
		errorState.groupedErrors.isEmpty() == true
		
		and: "The ungrouped errors in error state are empty"
		errorState.ungroupedErrors.isEmpty() == true
	}

	def "Scenario: create validation state with grouped errors"() {
		given: "The validation state with grouped errors"
		ValidationState validationState = new ValidationState()
			.withGrouped("group", "message");
		
		expect: "The validation state is invalid"
		ErrorState errorState = validationState.getErrorState()
		validationState.valid == false
		
		and: "The grouped errors in error state aren't empty"
		errorState.groupedErrors.isEmpty() == false
		
		and: "The grouped errors in error state contain known error message"
		errorState.groupedErrors.get("group") == Arrays.asList("message")
		
		and: "The ungrouped errors in error state are empty"
		errorState.ungroupedErrors.isEmpty() == true
	}

	def "Scenario: create validation state with ungrouped errors"() {
		given: "The validation state with ungrouped errors"
		ValidationState validationState = new ValidationState()
			.withUngrouped("message");
		
		expect: "The validation state is invalid"
		ErrorState errorState = validationState.getErrorState()
		validationState.valid == false
		
		and: "The grouped errors in error state are empty"
		errorState.groupedErrors.isEmpty() == true
		
		and: "The ungrouped errors in error state aren't empty"
		errorState.ungroupedErrors.isEmpty() == false
		
		and: "The ungrouped errors in error state contain known error message"
		errorState.ungroupedErrors.contains("message")
	}

	def "Scenario: create validation state with mixed types errors"() {
		given: "The validation state with grouped and ungrouped errors"
		ValidationState validationState = new ValidationState()
			.withGrouped("group", "grouped_message")
			.withUngrouped("ungrouped_message_1")
			.withUngrouped("ungrouped_message_2");
		
		expect: "The validation state is invalid"
		ErrorState errorState = validationState.getErrorState()
		validationState.valid == false
		
		and: "The grouped errors in error state aren't empty"
		errorState.groupedErrors.isEmpty() == false
		
		and: "The grouped errors in error state contain known error message"
		errorState.groupedErrors.get("group") == Arrays.asList("grouped_message")
				
		and: "The ungrouped errors in error state aren't empty"
		errorState.ungroupedErrors.isEmpty() == false
		
		and: "The ungrouped errors in error state contain known error message"
		errorState.ungroupedErrors.contains("ungrouped_message_1")
		errorState.ungroupedErrors.contains("ungrouped_message_2")
	}
	
	def "Scenario: check validation state for valid state"() {
		given: "The empty validation state"
		ValidationState validationState = new ValidationState();
		
		when: "The state validity has been checked"
		validationState.checkValidState(Severity.INCIDENT);
		
		then: "The validation exception hasn't been thrown"
		noExceptionThrown()
	}
	
	def "Scenario: check validation state for invalid state"() {
		given: "The non empty validation state"
		ValidationState validationState = new ValidationState()
			.withGrouped("group", "message")
			.withUngrouped("message");
			
		when: "The state validity has been checked"
		validationState.checkValidState(Severity.INCIDENT);
		
		then: "The validation exception has been thrown"
		ValidationException error = thrown() 
		
		and: "The error severity the same as we were sent"
		error.severity == Severity.INCIDENT
		
		and: "The known error context"
		error.context == ValidationException.VALIDATION_CONTEXT
		
		and: "The known error code"
		error.errorCode == 1L
		
		and: "The error state contains expected messages"
		error.errorState.groupedErrors.get("group") == Arrays.asList("message")
		error.errorState.ungroupedErrors.contains("message")
	}
}
