package io.bce.text

import io.bce.text.TextTemplate.Transformer
import spock.lang.Specification

class TextTemplatesSpec extends Specification {
  private static final Object RANDOM_OBJECT = new Object()
  private static final String RANDOM_STRING = "THE RANDOM STRING"
  private static final String CUSTOMIZED_NULL_PATTERN = "NULL"

  def "Scenario: create empty template"() {
    given: "The empty text template"
    TextTemplate textTemplate = TextTemplates.emptyTemplate()

    expect: "The template text should be empty string"
    textTemplate.getTemplateText().isEmpty() == true

    and: "The template parameters should be empty"
    textTemplate.getParameters().isEmpty() == true

    and: "The conversion to string result should be equivalent to the template text"
    textTemplate.toString() == textTemplate.getTemplateText()
  }

  def "Scenario: create template by an random object"() {
    given: "The text template created by the random object"
    TextTemplate textTemplate = TextTemplates.createBy(RANDOM_OBJECT)

    expect: "The template text should be converted to string object"
    textTemplate.getTemplateText() == RANDOM_OBJECT.toString()

    and: "The template parameters should be empty"
    textTemplate.getParameters().isEmpty() == true

    and: "The conversion to string result should be equivalent to the template text"
    textTemplate.toString() == textTemplate.getTemplateText()
  }

  def "Scenario: create template by a random string"() {
    given: "The text template created by the random string"
    TextTemplate textTemplate = TextTemplates.createBy(RANDOM_STRING)

    expect: "The template text should be the same random string"
    textTemplate.getTemplateText().is(RANDOM_STRING)

    and: "The template parameters should be empty"
    textTemplate.getParameters().isEmpty() == true

    and: "The conversion to string result should be equivalent to the template text"
    textTemplate.toString() == textTemplate.getTemplateText()
  }

  def "Scenario: create template by a template string and parameters"() {
    Map<String, Object> parameters = new HashMap<>()
    parameters.put("key", "value")

    given: "The text template created by a template string and parameters"
    TextTemplate textTemplate = TextTemplates.createBy(RANDOM_STRING, parameters)

    expect: "The template text should be the same random string"
    textTemplate.getTemplateText().is(RANDOM_STRING)

    and: "The template parameters should be equivalent to the input parameters map"
    textTemplate.getParameters() == parameters

    and: "The conversion to string result should be equivalent to the template text"
    textTemplate.toString() == textTemplate.getTemplateText()
  }

  def "Scenario: wrap an existing text template"() {
    Map<String, Object> parameters = new HashMap<>()
    parameters.put("key", "value")

    given: "The existing text template"
    TextTemplate sourceTemplate = Stub(TextTemplate)
    sourceTemplate.getTemplateText() >> RANDOM_STRING
    sourceTemplate.getParameters() >> parameters

    when: "The text template is wrapped"
    TextTemplate wrappedTemplate = TextTemplates.wrap(sourceTemplate)

    then: "The wrapped template text should be equivalent to the source template text"
    wrappedTemplate.getTemplateText() == sourceTemplate.getTemplateText()

    and: "The wrapped template parameters should be equivalent to the source parameters"
    wrappedTemplate.getParameters() == sourceTemplate.getParameters()

    and: "The conversion to string result should be equivalent to the template text"
    wrappedTemplate.toString() == wrappedTemplate.getTemplateText()
  }

  def "Scenario: transform by a transformer"() {
    given: "The default text template"
    TextTemplate sourceTemplate = TextTemplates.createBy(RANDOM_STRING)

    expect: "The text template should be correctly converted"
    TextTemplate convertedTemplate = sourceTemplate.transformBy({template ->
      return TextTemplates.createBy("CONVERTED(" + template.getTemplateText() + ");", template.getParameters())
    });

    convertedTemplate.getTemplateText() == "CONVERTED(" + RANDOM_STRING + ");"
  }

  def "Scenario: null-safety"() {
    given: "The text template initialized by null-values"
    TextTemplate textTemplate = TextTemplates.createBy(null, null)

    expect: "The template text should be equivalent to default null pattern(empty string)"
    textTemplate.getTemplateText() == ""

    and: "The parameters should be empty map"
    textTemplate.getParameters().isEmpty() == true
  }

  def "Scenario: customize null pattern"() {
    given: "The text template initialized by null template text with customized null pattern"
    TextTemplate textTemplate = TextTemplates.createBy(null).withNullPattern(CUSTOMIZED_NULL_PATTERN)

    expect: "The text template should be equivalent to the customized null pattern"
    textTemplate.getTemplateText() == CUSTOMIZED_NULL_PATTERN

    and: "The customization mustn't affect to the parameters set"
    textTemplate.getParameters().isEmpty() == true
  }

  def "Scenario: customize parameters map"() {
    given: "The text template with appended parameter"
    TextTemplate templateText = TextTemplates.createBy(RANDOM_STRING).withParameter("key", "value")

    expect: "The parameters set should contain appended parameter"
    templateText.getParameters().get("key") == "value"

    and: "The customization mustn't affect to the template text"
    templateText.getTemplateText() == RANDOM_STRING
  }
}
