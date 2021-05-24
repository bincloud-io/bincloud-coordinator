package io.bincloud.common.domain.model.message.templates

import org.junit.experimental.categories.Category

import io.bincloud.common.domain.model.message.MessageTemplate
import io.bincloud.common.domain.model.message.templates.StringifiedObjectTemplate
import spock.lang.Narrative
import spock.lang.Specification


@Narrative("""
	To represent random object as a message template, as a developer
	I am needed in a component which will adapt message template to 
	the java.lang.Object class. 
""")
class StringifiedObjectMessageSpec extends Specification {
	private static final Object WRAPPABLE_OBJECT = new Object()
	
	def "Scenario: wrap null value"() {
		given: "The wrapped null object"
			MessageTemplate messageTemplate = new StringifiedObjectTemplate(null)
		expect: "The message text should be empty string"
			messageTemplate.text == ""
		and: "The message parameters map should be empty"
			messageTemplate.parameters.isEmpty() == true
		and: "To string method should return empty string"
			messageTemplate.text == ""
	}
		
	def "Scenario: wrap object instance"() {
		given: "The wrapped null object"
			MessageTemplate messageTemplate = new StringifiedObjectTemplate(WRAPPABLE_OBJECT)
		expect: "The message text should return stringified object using toString method"
			messageTemplate.text == WRAPPABLE_OBJECT.toString()
		and: "The message parameters map is empty"
			messageTemplate.parameters.isEmpty() == true
		and: "To string method should return stringified object"
			messageTemplate.toString() == messageTemplate.text
	}
}
