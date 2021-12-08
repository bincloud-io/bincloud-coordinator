package io.bce.text.transformers

import io.bce.text.TextTemplate
import io.bce.text.TextTemplates
import io.bce.text.transformers.BundleResolvingTransformer.BundleResolver
import spock.lang.Specification

class BundleResolvingTransformerSpec extends Specification {
  def "Scenario: bundle is resolved"() {
    given: "The text template"
    TextTemplate template = TextTemplates.createBy("TEMPLATE.ID")

    and: "The bundle resolver successfully resolves the template by template id"
    BundleResolver resolver = Stub(BundleResolver)
    resolver.resolve("TEMPLATE.ID") >> Optional.of("RESOLVED_TEXT")

    when: "The transformation is applied"
    template = template.transformBy(new BundleResolvingTransformer(resolver))

    then: "The template text should be replaced by the resolved text"
    template.getTemplateText() == "RESOLVED_TEXT"
  }

  def "Scenario: bundle isn't resolved"() {
    given: "The text template"
    TextTemplate template = TextTemplates.createBy("TEMPLATE.ID")

    and: "The bundle resolver doesn't resolve the template by template id"
    BundleResolver resolver = Stub(BundleResolver)
    resolver.resolve("TEMPLATE.ID") >> Optional.empty()

    when: "The transformation is applied"
    template = template.transformBy(new BundleResolvingTransformer(resolver))

    then: "The template text shouldn't be changed"
    template.getTemplateText() == "TEMPLATE.ID"
  }

  def "Scenario: an error is happened during resolving"() {
    given: "The text template"
    TextTemplate template = TextTemplates.createBy("TEMPLATE.ID")

    and: "Something went wrong during resolving process"
    BundleResolver resolver = Stub(BundleResolver)
    resolver.resolve("TEMPLATE.ID") >> {throw new RuntimeException("SOMETHING WENT WRONG")}

    when: "The transformation is applied"
    template = template.transformBy(new BundleResolvingTransformer(resolver))

    then: "The template text shouldn't be changed"
    template.getTemplateText() == "TEMPLATE.ID"
  }
}
