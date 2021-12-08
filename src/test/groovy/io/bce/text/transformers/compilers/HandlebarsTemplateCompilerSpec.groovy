package io.bce.text.transformers.compilers

import io.bce.text.TextTemplate
import io.bce.text.TextTemplates
import io.bce.text.transformers.TemplateCompilingTransformer.TemplateCompiler
import spock.lang.Specification

class HandlebarsTemplateCompilerSpec extends Specification {
  private static final String MUSTACHE_TEMPLATE = "{{greetingWord}}, {{objectWord}}!!!"
  private static final String GREETING_WORD = "Hello"
  private static final String OBJECT_WORD = "World"

  def "Scenario: interpolate message template using mustache"() {
    Map<String, Object> parameters = new HashMap<>()
    parameters.put("greetingWord", GREETING_WORD)
    parameters.put("objectWord", OBJECT_WORD)

    given: "The handlebars template compiler"
    TemplateCompiler templateCompiler = new HandlebarsTemplateCompiler()

    expect: "The text should be interpolated correctly"
    templateCompiler.compile(MUSTACHE_TEMPLATE, parameters) == "Hello, World!!!"
  }
}
