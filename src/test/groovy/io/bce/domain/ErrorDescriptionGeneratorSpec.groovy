package io.bce.domain

import io.bce.domain.errors.ApplicationException
import io.bce.domain.errors.ErrorDescriptionGenerator
import io.bce.domain.errors.ErrorDescriptor.ErrorCode
import io.bce.domain.errors.ErrorDescriptor.ErrorSeverity
import io.bce.text.TextProcessor
import io.bce.text.TextTemplates
import spock.lang.Narrative
import spock.lang.Specification

class ErrorDescriptionGeneratorSpec extends Specification {
	private static final String PROCESSED_MESSAGE = "Processed message"
	private static final String DEFAULT_MESSAGE = "Something went wrong!!!"


	def "Scenario: generate message template using processor"() {
		given: "Text processor which could process message"
		TextProcessor textProcessor = TextProcessor.create().withTransformer({TextTemplates.createBy(PROCESSED_MESSAGE)})

		and: "The application error"
		ApplicationException error = new ApplicationException( BoundedContextId.createFor("CTX"), ErrorSeverity.BUSINESS, ErrorCode.createFor(1L), DEFAULT_MESSAGE)

		expect: "Processed message should be generated"
		ErrorDescriptionGenerator.of(textProcessor, error).generateDescription() == PROCESSED_MESSAGE
	}

	def "Scenario: generate default description"() {
		given: "Text processor which couldn't process message"
		TextProcessor textProcessor = TextProcessor.create()

		and: "The application error"
		ApplicationException error = new ApplicationException( BoundedContextId.createFor("CTX"), ErrorSeverity.BUSINESS, ErrorCode.createFor(1L), DEFAULT_MESSAGE)

		expect: "Default message should be generated"
		ErrorDescriptionGenerator.of(textProcessor, error).generateDescription() == error.getMessage()
	}
}
