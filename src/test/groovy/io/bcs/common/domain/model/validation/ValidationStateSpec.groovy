package io.bcs.common.domain.model.validation

import org.junit.experimental.categories.Category

import io.bcs.common.domain.model.validation.ValidationException
import io.bcs.common.domain.model.validation.ValidationService
import io.bcs.common.domain.model.validation.ValidationState
import io.bcs.common.domain.model.validation.ValidationState.ErrorState
import spock.lang.Specification

@Category(ValidationService)
class ValidationStateSpec extends Specification {
	public static final String VALIDATION_CONTEXT = "COMMON__VALIDATION";
	
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
		validationState.checkValidState(VALIDATION_CONTEXT, 1L);
		
		then: "The validation exception hasn't been thrown"
		noExceptionThrown()
	}
	
	def "Scenario: check validation state for invalid state"() {
		given: "The non empty validation state"
		ValidationState validationState = new ValidationState()
			.withGrouped("group", "message")
			.withUngrouped("message");
			
		when: "The state validity has been checked"
		validationState.checkValidState(VALIDATION_CONTEXT, 1L);
		
		then: "The validation exception has been thrown"
		ValidationException error = thrown() 
		
		and: "The known error context"
		error.getContext() == VALIDATION_CONTEXT
		
		and: "The known error code"
		error.getErrorCode() == 1L
		
		and: "The error state contains expected messages"
		error.errorState.groupedErrors.get("group") == Arrays.asList("message")
		error.errorState.ungroupedErrors.contains("message")
		
		and: "The error state details should be available by ${ValidationException.ERROR_STATE_PROPERTY} key"
		error.getDetails().get(ValidationException.ERROR_STATE_PROPERTY).is(error.getErrorState())
		
		and: "The wrong parameter names should be available by ${ValidationException.WRONG_PARAMETERS_PROPERTY} key"
		error.getDetails().get(ValidationException.WRONG_PARAMETERS_PROPERTY) == error.getErrorState().getGroupedErrors().keySet()
	}
	
	def "Scenario: append additional properties to the validation state"() {
		given: "The non valid validation state"
		ValidationState validationState = new ValidationState()
			.withUngrouped("error");
			
		when: "The additional property has been added"
		validationState = validationState.withAdditionalProperty("additionalProperty", "additional-value")
		
		and: "The validation state has been checked"
		validationState.checkValidState(VALIDATION_CONTEXT, 100)
		
		then: "The validation exception should be thrown"
		ValidationException error = thrown()
		
		and: "The validation exception should contain additional properties in the details"
		error.getDetails().get("additionalProperty") == "additional-value"
	}
}
