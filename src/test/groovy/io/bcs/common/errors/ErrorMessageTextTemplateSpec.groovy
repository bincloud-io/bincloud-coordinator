package io.bcs.common.errors

import io.bce.text.TextTemplate
import io.bce.text.TextTemplates
import io.bce.validation.ErrorMessage
import io.bcs.common.errors.ErrorMessageTextTemplate
import spock.lang.Specification

class ErrorMessageTextTemplateSpec extends Specification {
  def "Scenario: wrap error message"() {
    given: "The error message"
    ErrorMessage errorMessage = ErrorMessage.createFor("Error message").withParameter("key", "value")

    when: "The error message is decorated"
    TextTemplate textTemplate = new ErrorMessageTextTemplate(errorMessage)

    then: "The text template should provide template text the same as error message text"
    textTemplate.getTemplateText() == errorMessage.getMessage()

    and: "The text template should provide parameters the same as error message"
    textTemplate.getParameters() == errorMessage.getParameters()

    and: "The text template should be able to be transformed if it is requested"
    textTemplate.transformBy({template ->
      return TextTemplates.createBy("Decorated: ${template.getTemplateText()}")
    }).getTemplateText() == "Decorated: Error message"
  }
}
