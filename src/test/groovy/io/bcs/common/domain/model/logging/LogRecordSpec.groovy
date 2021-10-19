package io.bcs.common.domain.model.logging

import io.bcs.common.domain.model.error.ErrorDescriptor
import io.bcs.common.domain.model.logging.Level
import io.bcs.common.domain.model.logging.LogRecord
import io.bcs.common.domain.model.message.MessageTemplate
import io.bcs.common.domain.model.message.templates.ErrorDescriptorTemplate
import io.bcs.common.domain.model.message.templates.StringifiedObjectTemplate
import spock.lang.Narrative
import spock.lang.Specification

@Narrative("""
	To make logging simpler and be possible aggregate attributes related to logging 
	and work with them, as a developer I am needed in component which will aggregate 
	logging data and declare behavior to transform them. 
""")
class LogRecordSpec extends Specification {
	private static final Object RANDOM_OBJECT = new Object()
	private static final String RANDOM_TEXT = "RANDOM TEXT"
	private static final String PARAM_KEY = "KEY"
	private static final String PARAM_VALUE = "VALUE"
	private static final String ERROR_CONTEXT = "ERRCTX"
	private static final Long ERROR_CODE = 100L


	def "Scenario: initialize from random object"() {
		given: "The log record initialized by #logLevel level and random object"
		LogRecord logRecord = new LogRecord(logLevel, RANDOM_OBJECT)

		expect: "The timestamp should be initialized"
		logRecord.getTimestamp() != null

		and: "The log level should be initialized without changings"
		logRecord.getLevel() == logLevel

		and: "The log message text should be the same as object stringification result"
		logRecord.getMessageText() == RANDOM_OBJECT.toString()

		and: "The log message parameters map should be empty"
		logRecord.getMessageParameters().isEmpty() == true


		where:
		logLevel << [Level.CRITIC, Level.DEBUG, Level.ERROR, Level.INFO, Level.TRACE, Level.WARN]
	}

	def "Scenario: initialize from random message text"() {
		given: "The log record initialized by #logLevel level and random text"
		LogRecord logRecord = new LogRecord(logLevel, RANDOM_TEXT)

		expect: "The timestamp should be initialized"
		logRecord.getTimestamp() != null

		and: "The log level should be initialized without changings"
		logRecord.getLevel() == logLevel

		and: "The log message text should be the same as random text string"
		logRecord.getMessageText() == RANDOM_TEXT.toString()

		and: "The log message parameters map should be empty"
		logRecord.getMessageParameters().isEmpty() == true


		where:
		logLevel << [Level.CRITIC, Level.DEBUG, Level.ERROR, Level.INFO, Level.TRACE, Level.WARN]
	}

	def "Scenario: initialize from error descriptor"() {
		given: "The error descriptor"
		ErrorDescriptor errorDescriptor = createMockErrorDescriptor()

		and: "The log record initialized by #logLevel level and error descriptor"
		LogRecord logRecord = new LogRecord(logLevel, errorDescriptor)

		expect: "The timestamp should be initialized"
		logRecord.getTimestamp() != null

		and: "The log level should be initialized without changings"
		logRecord.getLevel() == logLevel

		and: "The log message text should be converted to the error descriptor message template "
		ErrorDescriptorTemplate  expectedTemplate = new ErrorDescriptorTemplate(errorDescriptor)
		logRecord.getMessageText() == expectedTemplate.getText()
		logRecord.getMessageParameters() == expectedTemplate.getParameters()

		where:
		logLevel << [Level.CRITIC, Level.DEBUG, Level.ERROR, Level.INFO, Level.TRACE, Level.WARN]
	}

	def "Scenario: initialize from message template"() {
		given: "The message template"
		MessageTemplate messageTemplate = createMockMessageTemplate()

		and: "The log record initialized by #logLevel level and error descriptor"
		LogRecord logRecord = new LogRecord(logLevel, messageTemplate)

		expect: "The timestamp should be initialized"
		logRecord.getTimestamp() != null

		and: "The log level should be initialized without changings"
		logRecord.getLevel() == logLevel

		and: "The log message template should be initialized without changings"
		logRecord.getMessageText() == RANDOM_TEXT
		logRecord.getMessageParameters().get(PARAM_KEY) == PARAM_VALUE
		
		where:
		logLevel << [Level.CRITIC, Level.DEBUG, Level.ERROR, Level.INFO, Level.TRACE, Level.WARN]
	}
	
	def "Scenario: transform message"() {
		given: "The source log record"
		LogRecord sourceRecord = new LogRecord(logLevel, RANDOM_OBJECT);
		
		when: "The message transformation has been requested"
		LogRecord transformedRecord = sourceRecord.transformMessage({new StringifiedObjectTemplate(RANDOM_TEXT)})

		then: "The new log record instance should be created based on source log record"
		sourceRecord.is(transformedRecord) == false
		
		and: "Their timestamps should be the same"
		sourceRecord.getTimestamp() == transformedRecord.getTimestamp()
		
		and: "Their levels should be the same"
		sourceRecord.getLevel() == transformedRecord.getLevel()
		
		and: "The message template should be converted"
		transformedRecord.getMessageText() == RANDOM_TEXT
				
		where:
		logLevel << [Level.CRITIC, Level.DEBUG, Level.ERROR, Level.INFO, Level.TRACE, Level.WARN]
	}

	private MessageTemplate createMockMessageTemplate() {
		MessageTemplate messageTemplate = Stub(MessageTemplate)
		messageTemplate.getText() >> RANDOM_TEXT
		Map<String, Object> parameters = new HashMap()
		messageTemplate.getParameters() >> parameters
		parameters.put(PARAM_KEY, PARAM_VALUE)
		return messageTemplate
	}

	private ErrorDescriptor createMockErrorDescriptor() {
		ErrorDescriptor errorDescriptor = Stub(ErrorDescriptor)
		errorDescriptor.getContext() >> ERROR_CONTEXT
		errorDescriptor.getErrorCode() >> ERROR_CODE
		Map<String, Object> parameters = new HashMap()
		errorDescriptor.getDetails() >> parameters
		parameters.put(PARAM_KEY, PARAM_VALUE)
		return errorDescriptor
	}
}
