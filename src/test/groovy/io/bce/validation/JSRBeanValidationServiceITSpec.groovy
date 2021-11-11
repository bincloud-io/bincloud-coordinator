package io.bce.validation

import java.util.stream.Collectors

import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory

import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator

import io.bce.text.TextProcessor
import io.bce.text.TextTemplate
import io.bce.text.transformers.TemplateCompilingTransformer
import io.bce.text.transformers.compilers.HandlebarsTemplateCompiler
import io.bce.validation.ValidationState.ErrorState
import spock.lang.Specification

class JSRBeanValidationServiceITSpec extends Specification {
	private TextProcessor messageProcessor
	private Validator validator

	def setup() {
		this.messageProcessor = TextProcessor.create().withTransformer(new TemplateCompilingTransformer(new HandlebarsTemplateCompiler()))
		ValidatorFactory validatorFactory = Validation.byDefaultProvider().configure()
				.messageInterpolator(new ParameterMessageInterpolator())
				.buildValidatorFactory()
		validator = validatorFactory.getValidator()
	}

	def "Scenario: validate object with validation framework"() {
		given: "The wrong bean"
		WrongBean wrongBean = new WrongBean()

		and: "The validation service"
		ValidationService validationService = new JSRBeanValidationService(validator, messageProcessor)

		when: "The validation has been requested"
		ErrorState errorState = validationService.validate(wrongBean).getErrorState()


		then: "The error state should contain ungrouped error with processed text"
		Collection<String> ungroupedErrors = stringifyTextTemplatesCollection(errorState.getUngroupedErrors())
		ungroupedErrors.containsAll([
			"Violation constraint: Ungrouped property"
		])

		and: "The error state should"
		Collection<String> groupedErrors = stringifyTextTemplatesCollection(GroupedErrors.errorsOf("stringValue", errorState))
		groupedErrors.containsAll([
			"Violation constraint: Grouped property"
		])
	}

	private Collection<String> stringifyTextTemplatesCollection(Collection<TextTemplate> templatesCollection) {
		return templatesCollection.stream().map({template -> template.toString()}).collect(Collectors.toList());
	}

	@AlwaysFailed(passedProperty="Ungrouped property", message="Violation constraint: {{passedProperty}}")
	class WrongBean {
		@AlwaysFailed(passedProperty="Grouped property", message="Violation constraint: {{passedProperty}}")
		private String stringValue;

		public WrongBean() {
			super();
			this.stringValue = "WRONG VALUE";
		}
	}
}
