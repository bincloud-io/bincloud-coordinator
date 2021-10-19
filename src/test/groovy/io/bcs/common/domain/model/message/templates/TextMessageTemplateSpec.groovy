package io.bcs.common.domain.model.message.templates

import org.junit.experimental.categories.Category

import io.bcs.common.domain.model.message.MessageTemplate
import io.bcs.common.domain.model.message.templates.TextMessageTemplate
import spock.lang.Narrative
import spock.lang.Specification

@Narrative("""
	To pass a text message template between components before interpolation, 
	as a developer I want to have component which would contain message template
	and allow manipulate with template parameters via template configurer 
""")
class TextMessageTemplateSpec extends Specification {
	private static final String TEMPLATE = "TEMPLATE_STRING"
	private static final String PARAM_NAME_1 = "FIRST_PARAMETER"
	private static final String PARAM_NAME_2 = "SECOND_PARAMETER"
	
	def "Scenario: create message template without parameters"() {
		given: "The message template created by default constructor"
		MessageTemplate  messageTemplate = new TextMessageTemplate(TEMPLATE)
		
		expect: "The message template should contain not changed template string"
		messageTemplate.text == TEMPLATE
		
		and: "The empty parameters map"
		messageTemplate.parameters.isEmpty() == true	
	}
	
	def "Scenario: configure message template with existing parameters"() {
		given: "The source message template parameters with"
		Map<String, Object>  sourceMessageTemplateParameters = new HashMap()
		sourceMessageTemplateParameters.put(PARAM_NAME_1, new Object())
		
		and: "The message template initialized by them"
		MessageTemplate messageTemplate = new TextMessageTemplate(TEMPLATE, sourceMessageTemplateParameters)
		
		expect: "The message template should contain not changed template string"
		messageTemplate.text == TEMPLATE
		
		and: "The message template should contain map by which it has been initialized"
		messageTemplate.parameters == sourceMessageTemplateParameters
	}
	
	def "Scenario: configure message template parameters at realtime"() {
		given: "The source message template parameters with"
		Map<String, Object>  sourceMessageTemplateParameters = new HashMap()
		sourceMessageTemplateParameters.put(PARAM_NAME_1, new Object())
		
		and: "The message template initialized by them"
		TextMessageTemplate messageTemplate = new TextMessageTemplate(TEMPLATE, sourceMessageTemplateParameters)
		
		when: "The new parameter has been added"
		messageTemplate = messageTemplate.withParameter(PARAM_NAME_2, new Object())
		
		and: "The initial parameter has been dropped"
		messageTemplate = messageTemplate.withoutParameter(PARAM_NAME_1)
		
		then: "The message template should contain not changed template string"
		messageTemplate.text == TEMPLATE
		
		and: "The initial parameter should be dropped"
		messageTemplate.parameters.containsKey(PARAM_NAME_1) == false
		
		and: "The new parameter should be added"
		messageTemplate.parameters.containsKey(PARAM_NAME_2) == true
	}
	
	def "Scenario: create text message template by a prototype message template"() {
		given: "The prototype message template"
		MessageTemplate prototype = Stub(MessageTemplate)
		prototype.getText() >> TEMPLATE
		Map<String, Object>  sourceMessageTemplateParameters = new HashMap()
		sourceMessageTemplateParameters.put(PARAM_NAME_1, "VALUE")
		prototype.getParameters() >> sourceMessageTemplateParameters
		
		when: "The text message template has been created by the prototype"
		MessageTemplate messageTemplateDuplicate = new TextMessageTemplate(prototype)
		
		then: "Both of message templates should be structural equivalent"
		messageTemplateDuplicate.getText() == prototype.getText()
		messageTemplateDuplicate.getParameters() == prototype.getParameters()
	}
}
