package io.bce.validation

import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory

import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator

import io.bce.validation.ValidationState.ErrorState
import spock.lang.Specification

class JsrBeanValidationServiceITSpec extends Specification {
  private Validator validator

  def setup() {
    ValidatorFactory validatorFactory = Validation.byDefaultProvider().configure()
        .messageInterpolator(new ParameterMessageInterpolator())
        .buildValidatorFactory()
    validator = validatorFactory.getValidator()
  }

  def "Scenario: validate object with validation framework"() {
    given: "The wrong bean"
    WrongBean wrongBean = new WrongBean()

    and: "The validation service"
    ValidationService validationService = new JsrBeanValidationService(validator)

    when: "The validation has been requested"
    ErrorState errorState = validationService.validate(wrongBean).getErrorState()


    then: "The error state should contain ungrouped error with processed text"
    ErrorMessage ungroupedMessage = errorState.getUngroupedErrors().iterator().next();
    ungroupedMessage.getMessage() == "Ungrouped violation constraint"
    ungroupedMessage.getParameters().get("passedProperty") == "Ungrouped property"

    and: "The error state should"
    ErrorMessage groupedMessage = GroupedErrors.errorsOf("stringValue", errorState).iterator().next()
    groupedMessage.getMessage() == "Grouped violation constraint"
    groupedMessage.getParameters().get("passedProperty") == "Grouped property"
  }

  @AlwaysFailed(passedProperty="Ungrouped property", message="Ungrouped violation constraint")
  class WrongBean {
    @AlwaysFailed(passedProperty="Grouped property", message="Grouped violation constraint")
    private String stringValue;

    public WrongBean() {
      super();
      this.stringValue = "WRONG VALUE";
    }
  }
}
