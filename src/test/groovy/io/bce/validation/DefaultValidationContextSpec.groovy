package io.bce.validation

import static io.bce.validation.ValidationContext.DerivationPolicy.DERIVE_GROUPES

import io.bce.text.TextTemplate
import io.bce.text.TextTemplates
import io.bce.validation.ValidationContext.Rule
import io.bce.validation.ValidationContext.Validatable
import io.bce.validation.ValidationContext.ValueProvider
import io.bce.validation.ValidationState.ErrorState
import spock.lang.Specification

class DefaultValidationContextSpec extends Specification {
	private static final String FIRST_LEVEL_GROUP_NAME = "gr-first";
	private static final String SECOND_LEVEL_GROUP_NAME = "gr-second";
	private static final String THIRD_LEVEL_GROUP_NAME = "gr-third";
	private static final String FOURTH_LEVEL_GROUP_NAME = "gr-fourth";
	private static final String ELEMENTS_COLLECTION_GROUP = "collection"

	private static final String DERIVED_FROM_FIRST_AND_SECOND_LEVEL_GROUP_NAME = "${FIRST_LEVEL_GROUP_NAME}.${SECOND_LEVEL_GROUP_NAME}";
	private static final String DERIVED_FROM_FIRST_AND_SECOND_AND_THIRD_LEVEL_GROUP_NAME = "${DERIVED_FROM_FIRST_AND_SECOND_LEVEL_GROUP_NAME}.${THIRD_LEVEL_GROUP_NAME}";
	private static final String DERIVED_COLLECTION_ITEM_UNGROUPED_ERRORS_GROUP_NAME = "${DERIVED_FROM_FIRST_AND_SECOND_LEVEL_GROUP_NAME}.${ELEMENTS_COLLECTION_GROUP}.[1]"
	private static final String DERIVED_COLLECTION_ITEM_GROUPED_ERRORS_GROUP_NAME = "${DERIVED_COLLECTION_ITEM_UNGROUPED_ERRORS_GROUP_NAME}.${FOURTH_LEVEL_GROUP_NAME}"

	private static final String MESSAGE_TEXT_1  = "Message text 1";
	private static final String MESSAGE_TEXT_2  = "Message text 2";
	private static final String MESSAGE_TEXT_3  = "Message text 3";
	private static final String MESSAGE_TEXT_4  = "Message text 4";
	private static final String MESSAGE_TEXT_5  = "Message text 5";
	private static final String MESSAGE_TEXT_6  = "Message text 6";
	private static final String MESSAGE_TEXT_7  = "Message text 7";
	private static final String MESSAGE_TEXT_8  = "Message text 8";
	private static final String MESSAGE_TEXT_9  = "Message text 9";
	private static final String MESSAGE_TEXT_10 = "Message text 10";

	private static final TextTemplate MESSAGE_TEMPLATE_1  = TextTemplates.createBy(MESSAGE_TEXT_1);
	private static final TextTemplate MESSAGE_TEMPLATE_2  = TextTemplates.createBy(MESSAGE_TEXT_2);
	private static final TextTemplate MESSAGE_TEMPLATE_3  = TextTemplates.createBy(MESSAGE_TEXT_3);
	private static final TextTemplate MESSAGE_TEMPLATE_4  = TextTemplates.createBy(MESSAGE_TEXT_4);
	private static final TextTemplate MESSAGE_TEMPLATE_5  = TextTemplates.createBy(MESSAGE_TEXT_5);
	private static final TextTemplate MESSAGE_TEMPLATE_6  = TextTemplates.createBy(MESSAGE_TEXT_6);
	private static final TextTemplate MESSAGE_TEMPLATE_7  = TextTemplates.createBy(MESSAGE_TEXT_7);
	private static final TextTemplate MESSAGE_TEMPLATE_8  = TextTemplates.createBy(MESSAGE_TEXT_8);
	private static final TextTemplate MESSAGE_TEMPLATE_9  = TextTemplates.createBy(MESSAGE_TEXT_9);
	private static final TextTemplate MESSAGE_TEMPLATE_10 = TextTemplates.createBy(MESSAGE_TEXT_10);

	private static final Object VALIDATABLE_VALUE = new Object()


	def "Scenario: append error messages into context"() {
		given: "The validation service"
		DefaultValidationContext context = new DefaultValidationContext()

		and: "The ungrouped error messages text templates"
		TextTemplate[] ungroupedErrorTextTemplates = (TextTemplate[])[
			MESSAGE_TEMPLATE_1,
			MESSAGE_TEMPLATE_2,
			MESSAGE_TEMPLATE_3
		]

		and: "The grouped error messages text templates"
		TextTemplate[] groupedErrorTextTemplates = (TextTemplate[])[
			MESSAGE_TEMPLATE_1,
			MESSAGE_TEMPLATE_2
		]

		when: "The ungrouped message templates is appended to the context"
		context = context.withErrors(ungroupedErrorTextTemplates)

		and: "The grouped message templates is appended to the context to the ${FIRST_LEVEL_GROUP_NAME} group"
		context = context.withErrors(FIRST_LEVEL_GROUP_NAME, groupedErrorTextTemplates)

		and: "The error state is built from the context"
		ValidationState validationState = context.getState()
		ErrorState errorState = validationState.getErrorState()

		then: "The error state should contain appended ungrouped message templates"
		errorState.getUngroupedErrors().containsAll([
			MESSAGE_TEMPLATE_1,
			MESSAGE_TEMPLATE_2,
			MESSAGE_TEMPLATE_3
		]) == true

		and: "The error state should contain error messages appended to the ${FIRST_LEVEL_GROUP_NAME} group"
		GroupedErrors.errorsOf(FIRST_LEVEL_GROUP_NAME, errorState).containsAll([
			MESSAGE_TEMPLATE_1,
			MESSAGE_TEMPLATE_2
		])
	}

	def "Scenario: check validation rules"() {
		given: "The validation context"
		DefaultValidationContext context = new DefaultValidationContext()

		and: "The value provider"
		ValueProvider valueProvider = {
			VALIDATABLE_VALUE
		}

		and: "The validation rule"
		Rule<Object> rule = Stub(Rule)
		rule.isAcceptableFor(VALIDATABLE_VALUE) >> isAcceptable
		rule.check(VALIDATABLE_VALUE) >> ungroupedErrors >> groupedErrors

		when: "The rule is accepted to whole project"
		context = context.withRule(valueProvider, rule)

		and: "The rule is accepted during to the specified group"
		context = context.withRule(FIRST_LEVEL_GROUP_NAME, valueProvider, rule)

		and: "The validation state is built from the context"
		ValidationState validationState = context.getState()
		ErrorState errorState = validationState.getErrorState()

		then: "The error state validity flag should be ${isValid}"
		validationState.isValid() == isValid

		and: "The ungrouped errors should be the ${ungroupedErrors}"
		errorState.getUngroupedErrors().containsAll(ungroupedErrors)

		and: "The grouped errors should be the ${groupedErrors}"
		GroupedErrors.errorsOf(FIRST_LEVEL_GROUP_NAME, errorState).containsAll(groupedErrors)

		where:
		isAcceptable          | ungroupedErrors                             | groupedErrors                             | isValid
		false                 | []                                          | []                                        | true
		true                  | []                                          | []                                        | true
		true                  | [MESSAGE_TEMPLATE_1, MESSAGE_TEMPLATE_2]    | []                                        | false
		true                  | []                                          | [MESSAGE_TEMPLATE_3, MESSAGE_TEMPLATE_4]  | false
		true                  | [MESSAGE_TEMPLATE_1, MESSAGE_TEMPLATE_2]    | [MESSAGE_TEMPLATE_3, MESSAGE_TEMPLATE_4]  | false
	}


	def "Scenario: validate internal validatable subobjects"() {
		given: "The validation context"
		DefaultValidationContext context = new DefaultValidationContext()

		and: "The object under validation"
		Validatable validatable = new RootEntity()

		when: "The validatable object is validated"
		context = validatable.validate(context)

		and: "The validation state is built from the context"
		ValidationState validationState = context.getState()
		ErrorState errorState = validationState.getErrorState()

		then: "The error state validity flag should be invalid"
		validationState.isValid() == false

		and: "The error state should be valid"
		errorState.getUngroupedErrors().containsAll([
			MESSAGE_TEMPLATE_1,
			MESSAGE_TEMPLATE_8
		])
		GroupedErrors.errorsOf(FIRST_LEVEL_GROUP_NAME, errorState).containsAll([
			MESSAGE_TEMPLATE_2,
			MESSAGE_TEMPLATE_3
		])
		GroupedErrors.errorsOf(DERIVED_FROM_FIRST_AND_SECOND_LEVEL_GROUP_NAME, errorState).containsAll([
			MESSAGE_TEMPLATE_4,
			MESSAGE_TEMPLATE_5
		])
		GroupedErrors.errorsOf(DERIVED_FROM_FIRST_AND_SECOND_AND_THIRD_LEVEL_GROUP_NAME, errorState).containsAll([
			MESSAGE_TEMPLATE_6,
			MESSAGE_TEMPLATE_7
		])
		GroupedErrors.errorsOf(DERIVED_COLLECTION_ITEM_UNGROUPED_ERRORS_GROUP_NAME, errorState).containsAll([
			MESSAGE_TEMPLATE_9
		])
		GroupedErrors.errorsOf(DERIVED_COLLECTION_ITEM_GROUPED_ERRORS_GROUP_NAME, errorState).containsAll([
			MESSAGE_TEMPLATE_10
		])
	}

	def "Scenario: apply validation service for objects isn't implementing validatable interface"() {
		expect: "The non-validatable objects should always be valid"
		DefaultValidationContext.createValidationService().validate(new Object()).isValid()
	}

	def "Scenario: apply validation service for objects implementing validatable interface"() {
		expect: "The validation should be applied to the validatable objects"
		DefaultValidationContext.createValidationService().validate(new RootEntity()).isValid() == false
	}

	class RootEntity implements Validatable {
		private FirstEntity firstEntity = new FirstEntity();
		private SecondEntity secondEntity = new SecondEntity();

		@Override
		public DefaultValidationContext validate(ValidationContext context) {
			return context
					.validate(FIRST_LEVEL_GROUP_NAME, firstEntity)
					.validate(FIRST_LEVEL_GROUP_NAME, secondEntity, DERIVE_GROUPES)
		}
	}

	class FirstEntity implements Validatable {
		private ThirdEntity thirdEntity = new ThirdEntity()

		@Override
		public DefaultValidationContext validate(ValidationContext context) {
			return context
					.withErrors(MESSAGE_TEMPLATE_2)
					.validate(thirdEntity)
		}
	}

	class ThirdEntity implements Validatable {
		@Override
		public DefaultValidationContext validate(ValidationContext context) {
			return context
					.withErrors(MESSAGE_TEMPLATE_3)
					.withErrors(SECOND_LEVEL_GROUP_NAME, MESSAGE_TEMPLATE_4, MESSAGE_TEMPLATE_5)
		}
	}

	class SecondEntity implements Validatable {
		private FourthEntity fourthEntity = new FourthEntity()

		@Override
		public DefaultValidationContext validate(ValidationContext context) {
			return context
					.withErrors(MESSAGE_TEMPLATE_1)
					.validate(fourthEntity, DERIVE_GROUPES)
		}
	}

	class FourthEntity implements Validatable {
		private FifthEntity fifthEntity = new FifthEntity();

		@Override
		public DefaultValidationContext validate(ValidationContext context) {
			return context
					.validate(SECOND_LEVEL_GROUP_NAME, fifthEntity, DERIVE_GROUPES)
		}
	}

	class FifthEntity implements Validatable {
		private Collection<Object> firstCollection = [
			new Object(),
			new SixthEntity()
		]

		@Override
		public DefaultValidationContext validate(ValidationContext context) {
			return context
					.withErrors(MESSAGE_TEMPLATE_8)
					.withErrors(THIRD_LEVEL_GROUP_NAME, MESSAGE_TEMPLATE_6, MESSAGE_TEMPLATE_7)
					.validate(ELEMENTS_COLLECTION_GROUP, firstCollection)
		}
	}

	class SixthEntity implements Validatable {
		@Override
		public ValidationContext validate(ValidationContext context) {
			return context
				.withErrors(MESSAGE_TEMPLATE_9)
				.withErrors(FOURTH_LEVEL_GROUP_NAME, MESSAGE_TEMPLATE_10);
		}
	}
}
