package io.bce.text

import spock.lang.Specification

class TextSpec extends Specification {
  def "Scenario: register the text processor and interpolate text"() {
    given: "The text template"
    TextTemplate template = TextTemplates.createBy("Hello")

    and: "The text processor with registered transformer"
    TextProcessor textProcessor = TextProcessor.create()
        .withTransformer({TextTemplate source ->
          TextTemplates
              .createBy(String.format("%s world!", source.getTemplateText()))
        })

    when: "The text processor is registered into the global text utility"
    Text.configureProcessor(textProcessor)

    and: "The text message is interpolated using the util"
    String interpolatedByUtil = Text.interpolate(template)

    then: "The result should be the same as after text processor interpolation"
    textProcessor.interpolate(template) == interpolatedByUtil
  }

  def cleanup() {
    Text.reset()
  }
}
