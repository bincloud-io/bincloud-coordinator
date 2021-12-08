package io.bce.text

import spock.lang.Specification

class TextProcessorSpec extends Specification {
  def "Scenario: interpolate message"() {
    given: "The text template"
    TextTemplate template = TextTemplates.createBy("Hello")

    and: "The text processor with registered transformer"
    TextProcessor textProcessor = TextProcessor.create()
        .withTransformer({TextTemplate source ->
          TextTemplates
              .createBy(String.format("%s world!", source.getTemplateText()))
        })

    expect: "The text processor should correctly interpolate the message"
    textProcessor.interpolate(template) == "Hello world!"
  }
}
