package io.bce.text.transformers

import java.util.stream.Collectors

import groovy.mock.interceptor.StrictExpectation
import io.bce.text.TextTemplate
import io.bce.text.TextTemplates
import io.bce.text.transformers.BundleResolvingTransformer.BundleResolver
import io.bce.text.transformers.TemplateCompilingTransformer.TemplateCompiler
import spock.lang.Specification

class TemplateCompilingTransformerSpec extends Specification {
  def "Scenario: bundle is resolved"() {
    given: "The text template"
    TextTemplate template = TextTemplates.createBy("TEMPLATE.TEXT")
        .withParameter("PARAM_1", "VALUE_1")
        .withParameter("PARAM_2", "VALUE_2")

    and: "The template compiler resolving template"
    TemplateCompiler compiler = {templateText, parameters ->
      return String.format("%s[%s]", templateText, parameters.entrySet().stream()
          .map({entry -> String.format("%s=%s", entry.key, entry.value)})
          .collect(Collectors.joining(",")))
    }

    when: "The transformation is applied"
    template = template.transformBy(new TemplateCompilingTransformer(compiler))

    then: "The template text should be replaced by the resolved text"
    template.getTemplateText() == "TEMPLATE.TEXT[PARAM_1=VALUE_1,PARAM_2=VALUE_2]"

    and: "The template parameters shouldn't be transformed"
    template.getParameters().get("PARAM_1") == "VALUE_1"
    template.getParameters().get("PARAM_2") == "VALUE_2"
  }

  def "Scenario: bundle isn't resolved"() {
    given: "The text template"
    TextTemplate template = TextTemplates.createBy("TEMPLATE.TEXT")
        .withParameter("PARAM_1", "VALUE_1")
        .withParameter("PARAM_2", "VALUE_2")

    and: "The template compiler return null"
    TemplateCompiler compiler = {templateText, parameters -> null}

    when: "The transformation is applied"
    template = template.transformBy(new TemplateCompilingTransformer(compiler))


    then: "The template text should return empty string"
    template.getTemplateText() == ""

    and: "The template parameters shouldn't be transformed"
    template.getParameters().get("PARAM_1") == "VALUE_1"
    template.getParameters().get("PARAM_2") == "VALUE_2"
  }
}
