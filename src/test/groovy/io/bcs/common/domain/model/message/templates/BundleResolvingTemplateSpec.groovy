package io.bcs.common.domain.model.message.templates

import io.bcs.common.domain.model.message.MessageTemplate
import io.bcs.common.domain.model.message.templates.BundleResolvingTemplate
import io.bcs.common.domain.model.message.templates.BundleResolvingTemplate.BundleResolver
import spock.lang.Narrative
import spock.lang.Specification

@Narrative("""
	To have possibility represent message template with recursively resolved template 
	identifiers, as a developer I am needed in a component which will wrap message 
    template and do recursive parameters interpolation "under the skin".
""")
class BundleResolvingTemplateSpec extends Specification {
	private static final String SOURCE_TEXT = "SOURCE_TEXT"
	private static final String RESOLVED_TEXT = "RESOLVED_TEXT"
	private static final String SOURCE_PARAMETER_TEXT = "SOURCE_PARAMETER_TEXT"
	private static final String RESOLVED_PARAMETER_TEXT = "RESOLVED_PARAMETER_TEXT"
	
	def "Scenario: process message for case when bundle is resolved"() {
		given: "The message template"
		MessageTemplate messageTemplate = createMessageTemplate()
		
		and: "The message resolver which will resolve text"
		BundleResolver bundleResolver = Stub(BundleResolver)
		bundleResolver.resolveBundle(SOURCE_TEXT) >> Optional.of(RESOLVED_TEXT)
		bundleResolver.resolveBundle(SOURCE_PARAMETER_TEXT) >> Optional.of(RESOLVED_PARAMETER_TEXT)
		
		when: "The bundle resolving message template has been created from these ones"
		messageTemplate = new BundleResolvingTemplate(messageTemplate, bundleResolver)
		
		then: "The message text should be resolved"
		messageTemplate.getText() == RESOLVED_TEXT
		
		and: "The parameter text should be resolved"
		messageTemplate.getParameters().get("param").getText() == RESOLVED_PARAMETER_TEXT
	}
	
	def "Scenario: process message for case when bundle isn't resolved"() {
		given: "The message template"
		MessageTemplate messageTemplate = createMessageTemplate()
		
		and: "The message resolver which won't resolve text"
		BundleResolver bundleResolver = Stub(BundleResolver)
		bundleResolver.resolveBundle(SOURCE_TEXT) >> Optional.empty()
		bundleResolver.resolveBundle(SOURCE_PARAMETER_TEXT) >> Optional.empty()
		
		when: "The bundle resolving message template has been created from these ones"
		messageTemplate = new BundleResolvingTemplate(messageTemplate, bundleResolver)
		
		then: "The message text should be resolved"
		messageTemplate.getText() == SOURCE_TEXT
		
		and: "The parameter text should be resolved"
		messageTemplate.getParameters().get("param").getText() == SOURCE_PARAMETER_TEXT
	}
	
	private MessageTemplate createMessageTemplate() {
		MessageTemplate messageTemplate = Stub(MessageTemplate)
		MessageTemplate parameterTemplate = Stub(MessageTemplate)
		Map<String, Object> parameters = new HashMap();
		parameters.put("param", parameterTemplate)
		messageTemplate.getText() >> SOURCE_TEXT
		messageTemplate.getParameters() >> parameters
		parameterTemplate.getText() >> SOURCE_PARAMETER_TEXT
		parameterTemplate.getParameters() >> new HashMap()
		return messageTemplate
	}
}
