package io.bcs.common.port.adapters.message

import io.bcs.common.domain.model.message.MessageInterpolator
import io.bcs.common.domain.model.message.MessageTemplate
import io.bcs.common.domain.model.message.templates.TextMessageTemplate
import io.bcs.common.port.adapters.messages.MustacheInterpolator
import spock.lang.Narrative
import spock.lang.Specification

@Narrative("""
	To have mustache template with handlebar tool interpolation for message templates,
	as a developer I want to have corresponding interpolator
""")
class MustacheMessageInterpolatorSpec extends Specification {
	private static final String MUSTACHE_TEMPLATE = "{{greetingWord}}, {{objectWord}}!!!"
	private static final String GREETING_WORD = "Hello"
	private static final String OBJECT_WORD = "World"

	def "Scenario: interpolate message template using mustache"() {
		given: "The message template"
		MessageTemplate template = new TextMessageTemplate(MUSTACHE_TEMPLATE)
			.withParameter("greetingWord", GREETING_WORD)
			.withParameter("objectWord", OBJECT_WORD);
			
			
		and: "The mustache template tool based message interpolator"
		MessageInterpolator interpolator = new MustacheInterpolator()
		
		expect: "The text should be interpolated correctly"
		interpolator.interpolate(template) == "Hello, World!!!"
	}
}
