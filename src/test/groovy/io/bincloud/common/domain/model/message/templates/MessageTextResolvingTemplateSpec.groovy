package io.bincloud.common.domain.model.message.templates

import org.codehaus.groovy.runtime.powerassert.SourceText

import io.bincloud.common.domain.model.message.MessageInterpolator
import io.bincloud.common.domain.model.message.MessageTemplate
import io.bincloud.common.domain.model.message.templates.MessageTextResolvingTemplate
import spock.lang.Specification

class MessageTextResolvingTemplateSpec extends Specification {
	
	
	
	def "Scenario: resolve message template using interpolator"() {
		given: "The source message template"
		MessageTemplate messageTemplate = createSourceMessage()
		
		and: "The message interpolator"
		MessageInterpolator messageInterpolator = Stub(MessageInterpolator)
		messageInterpolator.interpolate(_) >> {
			MessageTemplate template = it[0]
			return String.format("RESOLVED.%s.[%s]", template.getText(), template.getParameters())
		}
		
		when: "The message template has been wrapped by message text resolving template"
		messageTemplate = new MessageTextResolvingTemplate(messageTemplate, messageInterpolator)
		
		then: "The parameters should be recursively resolved by interpolator"	
		messageTemplate.getParameters() == createResolvedMap()
		
		and: "The text message should be resolved by interpolator"
		messageTemplate.getText() == createResolvedMessageText()
	}
	
	private MessageTemplate createSourceMessage() {
		MessageTemplate sourceTemplate = Stub(MessageTemplate)
		sourceTemplate.getText() >> "SOURCE_TEXT"
		sourceTemplate.getParameters() >> createSourceMap()
		return sourceTemplate
	}
	
	private String createResolvedMessageText() {
		Map<String, Object> resolvedMap = createResolvedMap()
		return String.format("RESOLVED.SOURCE_TEXT.[%s]", resolvedMap)
	}
	
	private Map<String, Object> createSourceMap() {
		Map<String, Object> result = new HashMap<>()
		result.put("firstParameter", createInnerMessageTemplate())
		result.put("secondParameter", "STATIC_PARAMETER")
		return result
	}
	
	private Map<String, Object> createResolvedMap() {
		Map<String, Object> result = new HashMap<>()
		result.put("firstParameter", String.format("RESOLVED.INNER_PARAMETER.[%s]", new HashMap<>()) )
		result.put("secondParameter", "STATIC_PARAMETER")
		return result
	}
	
	private MessageTemplate createInnerMessageTemplate() {
		MessageTemplate result = Stub(MessageTemplate)
		result.getText() >> "INNER_PARAMETER"
		result.getParameters() >> new HashMap<>()
		return result
	}
}
