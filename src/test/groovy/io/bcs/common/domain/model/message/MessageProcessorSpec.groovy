package io.bcs.common.domain.model.message

import io.bcs.common.domain.model.message.MessageProcessor
import io.bcs.common.domain.model.message.MessageTemplate
import io.bcs.common.domain.model.message.MessageTransformer
import spock.lang.Narrative
import spock.lang.Specification

@Narrative("""
	To make work with messages simpler and encapsulate logic of processing steps "under
	the skin" into single component, as a developer I am needed in corresponding component
	which will provide simple API for run messages templates transformations and API for
	its configuring. Finally we will have single component which will handle of messages
	transformation and interpolation process.
""")
class MessageProcessorSpec extends Specification {
	def "Scenario: process message template"() {
		given: "The source message template"
		MessageTemplate sourceTemplate = createSourceTemplate()

		and: "The message transformer transforms the source message"
		MessageTransformer transformer = createTransformer(sourceTemplate)

		and: "Pre configured message processor component"
		MessageProcessor messageProcessor = new MessageProcessor().configure()
				.withTransformation(transformer)
				.apply();
				
		when: "The message processing has been requested"
		MessageTemplate resultTemplate = messageProcessor.process(sourceTemplate)
		
		then: "The message text should be different of the source template"
		resultTemplate.text != sourceTemplate.text
		
		and: "The message text parameters should be different of the source template"
		resultTemplate.parameters != sourceTemplate.parameters
		
		and: "The processed template should be the same returned by transformer"
		resultTemplate.text == "TRANFORMED"
		resultTemplate.parameters.get("KEY") == "VALUE"
	}
	
	def "Scenario: interpolate message template"() {
		given: "The source message template"
		MessageTemplate sourceTemplate = createSourceTemplate()

		and: "The message transformer transforms the source message"
		MessageTransformer transformer = createTransformer(sourceTemplate)

		and: "Pre configured message processor component"
		MessageProcessor messageProcessor = new MessageProcessor().configure()
				.withTransformation(transformer)
				.apply();
				
		when: "The message interpolation has been requested"
		MessageTemplate resultTemplate = messageProcessor.process(sourceTemplate)
		
		then: "The message text should be the same as transformed template text"
		resultTemplate.text == "TRANFORMED"
		
	}

	private MessageTransformer createTransformer(MessageTemplate sourceTemplate) {
		MessageTransformer transformer = Stub(MessageTransformer);
		transformer.transformMessage(sourceTemplate) >> createTransformedTemplate()
		return transformer;
	}

	private MessageTemplate createTransformedTemplate() {
		MessageTemplate transformedTemplate = Stub(MessageTemplate)
		Map<String, Object> parametersMap = new HashMap();
		parametersMap.put("KEY", "VALUE");
		transformedTemplate.getText() >> "TRANFORMED";
		transformedTemplate.getParameters() >> parametersMap;
		return transformedTemplate;
	}

	private MessageTemplate createSourceTemplate() {
		MessageTemplate sourceTemplate = Stub(MessageTemplate);
		sourceTemplate.getText() >> "SOURCE";
		sourceTemplate.getParameters() >> new HashMap<>();
		return sourceTemplate;
	}
}
