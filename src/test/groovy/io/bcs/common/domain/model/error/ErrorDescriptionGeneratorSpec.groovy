package io.bcs.common.domain.model.error

import io.bcs.common.domain.model.error.ApplicationException
import io.bcs.common.domain.model.error.ErrorDescriptionGenerator
import io.bcs.common.domain.model.error.ApplicationException.Severity
import io.bcs.common.domain.model.message.MessageProcessor
import io.bcs.common.domain.model.message.templates.TextMessageTemplate
import spock.lang.Narrative
import spock.lang.Specification

@Narrative("""
	To make errors description generation simpler, as a developer, I am needed in
	the component which will receive exception object and generate description for ]
	this. It will allow avoid error description generation duplication.
""")
class ErrorDescriptionGeneratorSpec extends Specification {
	private static final String PROCESSED_MESSAGE = "Processed message"
	private static final String DEFAULT_MESSAGE = "Something went wrong!!!"
	
	
	def "Scenario: generate message template using processor"() {
		given: "Message processor which could process message"
		MessageProcessor messageProcessor = new MessageProcessor().configure()
				.withTransformation({new TextMessageTemplate(PROCESSED_MESSAGE)})
				.apply()
				
		and: "The application error"
		ApplicationException error = new ApplicationException(Severity.BUSINESS, "CTX", 1L, DEFAULT_MESSAGE)
		
		expect: "Processed message should be generated"
		ErrorDescriptionGenerator.of(messageProcessor, error).generateDescription() == PROCESSED_MESSAGE
	}
	
	def "Scenario: generate default description"() {
		given: "Message processor which couldn't process message"
		MessageProcessor messageProcessor = new MessageProcessor();
		
		and: "The application error"
		ApplicationException error = new ApplicationException(Severity.BUSINESS, "CTX", 1L, DEFAULT_MESSAGE)
		
		expect: "Default message should be generated"
		ErrorDescriptionGenerator.of(messageProcessor, error).generateDescription() == error.getMessage()
	}
}
