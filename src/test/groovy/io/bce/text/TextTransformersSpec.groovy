package io.bce.text

import java.util.stream.Collectors

import io.bce.text.TextTemplate.Transformer
import spock.lang.Specification

class TextTransformersSpec extends Specification {
  private static final String TEMPLATE_TEXT_WITH_WHITESPACE = "  TEMPLATE            "
  private static final String TEMPLATE_TEXT_WITHOUT_WHITESPACE = "TEMPLATE"

  def "Scenario: apply the trimming transformer"() {
    given: "The text template with whitespaces at the start and at the end of template text"
    TextTemplate textTemplate = TextTemplates.createBy(TEMPLATE_TEXT_WITH_WHITESPACE)

    when: "The trimming transformer is applied"
    textTemplate = textTemplate.transformBy(TextTransformers.trimming())

    then: "The template text should be trimmed"
    textTemplate.getTemplateText() == TEMPLATE_TEXT_WITHOUT_WHITESPACE
  }

  def "Scenario: apply the chaining transformer"() {
    given: "The text template"
    TextTemplate textTemplate = TextTemplates.createBy("TEMPLATE_TEXT")

    and: "The first transformation"
    Transformer firstTransformation = {template ->
      return TextTemplates.createBy(String.format("%s.FIRST_TRANSFORMATION", template.getTemplateText()), template.getParameters())
    }

    and: "The second transformation"
    Transformer secondTransformation = {template ->
      return TextTemplates.createBy(String.format("%s.NEXT_TRANSFORMATION", template.getTemplateText()), template.getParameters())
    }

    when: "The chaining transformer is applied"
    textTemplate = textTemplate.transformBy(TextTransformers.chain(firstTransformation, secondTransformation))

    then: "The transformation should be applied in the correct order"
    textTemplate.getTemplateText() == "TEMPLATE_TEXT.FIRST_TRANSFORMATION.NEXT_TRANSFORMATION"
  }

  def "Scenario: apply the deep dive transformer"() {
    given: "The text template"
    TextTemplate textTemplate = TextTemplates.createBy("ROOT")
        .withParameter("SIMPLE_PARAM", "SIMPLE_PARAM_VALUE")
        .withParameter("TEMPLATE_PARAM", TextTemplates.createBy("PARAM_TEMPLATE")
        .withParameter("PARAM_TEMPLATE_KEY", "PARAM_TEMPLATE_VALUE"))

    and: "The non-recursive transformation"
    Transformer transformer = {template ->
      String concatinatedParams = template.getParameters().entrySet().stream()
          .map({entry -> String.format("%s=%s", entry.key, entry.value)})
          .collect(Collectors.joining(","))

      return TextTemplates.createBy(String.format("%s[%s]", template.getTemplateText(), concatinatedParams), template.getParameters())
    }

    when: "The deep-dive transformation is applied for non recursive pattern"
    textTemplate = textTemplate.transformBy(TextTransformers.deepDive(transformer))

    then: "The non-recursive transformation should be applied deeply including text template parameters"
    textTemplate.getTemplateText() == "ROOT[SIMPLE_PARAM=SIMPLE_PARAM_VALUE,TEMPLATE_PARAM=PARAM_TEMPLATE[PARAM_TEMPLATE_KEY=PARAM_TEMPLATE_VALUE]]" ||
        textTemplate.getTemplateText() == "ROOT[TEMPLATE_PARAM=PARAM_TEMPLATE[PARAM_TEMPLATE_KEY=PARAM_TEMPLATE_VALUE],SIMPLE_PARAM=SIMPLE_PARAM_VALUE]"

    and: "The parameters should be modified"
    textTemplate.getParameters().get("SIMPLE_PARAM") == "SIMPLE_PARAM_VALUE"
    textTemplate.getParameters().get("TEMPLATE_PARAM") == "PARAM_TEMPLATE[PARAM_TEMPLATE_KEY=PARAM_TEMPLATE_VALUE]"
  }
}
