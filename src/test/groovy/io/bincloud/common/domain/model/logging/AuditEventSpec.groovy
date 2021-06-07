package io.bincloud.common.domain.model.logging

import io.bincloud.common.domain.model.error.ErrorDescriptor
import io.bincloud.common.domain.model.logging.Level
import io.bincloud.common.domain.model.logging.LogRecord
import io.bincloud.common.domain.model.logging.audit.ServiceAuditEvent
import io.bincloud.common.domain.model.message.MessageTemplate
import io.bincloud.common.domain.model.message.templates.ErrorDescriptorTemplate
import io.bincloud.common.domain.model.message.templates.StringifiedObjectTemplate
import spock.lang.Narrative
import spock.lang.Specification

@Narrative("""
	To make audit logging simpler and be possible aggregate attributes related
	to audit logging (don't confuse with system logging), as a developer I am 
	needed in component which will aggregate audit event data and declare behavior 
	to transform them.
""")
class AuditEventSpec extends Specification {
	private static final String RANDOM_TEXT = "RANDOM TEXT"
	private static final String PARAM_KEY_1 = "KEY_1"
	private static final String PARAM_VALUE_1 = "VALUE_1"
	private static final String PARAM_KEY_2 = "KEY_2"
	private static final String PARAM_VALUE_2 = "VALUE_2"
	private static final String EVENT_CONTEXT = "ERRCTX"
	private static final Long ERROR_CODE = 100L
	private static final Collection<String> AUDIT_DETAILS_PARAMETERS = Arrays.asList(PARAM_KEY_2)


	def "Scenario: initalize from event code and message template"() {
		given: "The audit event initialized with #logLevel log level by event code and message template"
		ServiceAuditEvent auditEvent = new ServiceAuditEvent(EVENT_CONTEXT, logLevel, createMockMessageTemplate(), AUDIT_DETAILS_PARAMETERS)

		expect:  "The timestamp should be initialized"
		auditEvent.getAuditLogTimestamp() != null

		and: "The log level should be initialized without changings"
		auditEvent.getAuditLogLevel() == logLevel

		and: "The log message text should be initialized from message template"
		auditEvent.getAuditLogMessageText() == RANDOM_TEXT

		and: "Audit details parameters should be strictly contain parameters with names enumerated in audit parameter names"
		auditEvent.getAuditDetailsParameters().get(PARAM_KEY_2) == PARAM_VALUE_2
		auditEvent.getAuditDetailsParameters().containsKey(PARAM_KEY_1) == false

		and: "The event context should be initialized without changings"
		auditEvent.getEventCode() == EVENT_CONTEXT

		and: "The error code should be zero"
		auditEvent.getErrorCode() == 0L

		where:
		logLevel << [Level.CRITIC, Level.DEBUG, Level.ERROR, Level.INFO, Level.TRACE, Level.WARN]
	}

	def "Scenario: initialize from error descriptor"() {
		given: "The audit event initialized with #logLevel log level by error descriptor"
		ServiceAuditEvent auditEvent = new ServiceAuditEvent(logLevel, createMockErrorDescriptor(), AUDIT_DETAILS_PARAMETERS)

		expect:  "The timestamp should be initialized"
		auditEvent.getAuditLogTimestamp() != null

		and: "The log level should be initialized without changings"
		auditEvent.getAuditLogLevel() == logLevel

		and: "The log message text should be initialized from error descriptor"
		ErrorDescriptorTemplate  expectedTemplate = new ErrorDescriptorTemplate(createMockErrorDescriptor())
		auditEvent.getAuditLogMessageText() == expectedTemplate.getText()

		and: "Audit details parameters should be strictly contain parameters with names enumerated in audit parameter names"
		auditEvent.getAuditDetailsParameters().get(PARAM_KEY_2) == PARAM_VALUE_2
		auditEvent.getAuditDetailsParameters().containsKey(PARAM_KEY_1) == false

		and: "The event context should be initialized from error descriptor"
		auditEvent.getEventCode() == EVENT_CONTEXT

		and: "The error code  should be initialized from error descriptor"
		auditEvent.getErrorCode() == ERROR_CODE

		where:
		logLevel << [Level.CRITIC, Level.DEBUG, Level.ERROR, Level.INFO, Level.TRACE, Level.WARN]
	}

	def "Scenario: transform message"() {
		given: "The source audit event"
		ServiceAuditEvent sourceEvent = new ServiceAuditEvent(EVENT_CONTEXT, logLevel, createMockMessageTemplate(), AUDIT_DETAILS_PARAMETERS)

		when: "The message transformation has been requested"
		ServiceAuditEvent transformedEvent = sourceEvent.transformMessage({new StringifiedObjectTemplate("TRANSFORMED_TEXT")})

		then: "The new audit event instance should be created based on source audit event"
		transformedEvent.is(sourceEvent) == false

		and: "Their log levels should be the same"
		transformedEvent.getAuditLogLevel() == sourceEvent.getAuditLogLevel()

		and: "Their timestamps should be the same"
		transformedEvent.getAuditLogTimestamp() == sourceEvent.getAuditLogTimestamp()

		and: "Their event codes should be the same"
		transformedEvent.getEventCode() == sourceEvent.getEventCode()

		and: "Their error codes should be the same"
		transformedEvent.getErrorCode() == sourceEvent.getErrorCode()

		and: "Their audit message should be transformed"
		transformedEvent.getAuditLogMessageText() != sourceEvent.getAuditLogMessageText()

		where:
		logLevel << [Level.CRITIC, Level.DEBUG, Level.ERROR, Level.INFO, Level.TRACE, Level.WARN]
	}

	def "Scenario: get audit log record"() {
		given: "The audit event"
		ServiceAuditEvent sourceEvent = new ServiceAuditEvent(EVENT_CONTEXT, logLevel, createMockMessageTemplate(), AUDIT_DETAILS_PARAMETERS)

		when: "The audit log record has been requested"
		LogRecord auditLogRecord = sourceEvent.getAuditLogRecord()

		then: "Log record timestamp should be equal to audit event timestamp"
		auditLogRecord.getTimestamp() == sourceEvent.getAuditLogTimestamp()

		and: "Log record level should be equal to audit event log level"
		auditLogRecord.getLevel() == sourceEvent.getAuditLogLevel()

		and: "Log record message data should be equal to audit event message data"
		auditLogRecord.getMessageText() == sourceEvent.getAuditLogMessageText()
		auditLogRecord.getMessageParameters().get(PARAM_KEY_1) == PARAM_VALUE_1
		auditLogRecord.getMessageParameters().get(PARAM_KEY_2) == PARAM_VALUE_2
		
		
		where:
		logLevel << [Level.CRITIC, Level.DEBUG, Level.ERROR, Level.INFO, Level.TRACE, Level.WARN]
	}

	private MessageTemplate createMockMessageTemplate() {
		MessageTemplate messageTemplate = Stub(MessageTemplate)
		messageTemplate.getText() >> RANDOM_TEXT
		Map<String, Object> parameters = new HashMap()
		messageTemplate.getParameters() >> parameters
		parameters.put(PARAM_KEY_1, PARAM_VALUE_1)
		parameters.put(PARAM_KEY_2, PARAM_VALUE_2)
		return messageTemplate
	}

	private ErrorDescriptor createMockErrorDescriptor() {
		ErrorDescriptor errorDescriptor = Stub(ErrorDescriptor)
		errorDescriptor.getContext() >> EVENT_CONTEXT
		errorDescriptor.getErrorCode() >> ERROR_CODE
		Map<String, Object> parameters = new HashMap()
		errorDescriptor.getDetails() >> parameters
		parameters.put(PARAM_KEY_1, PARAM_VALUE_1)
		parameters.put(PARAM_KEY_2, PARAM_VALUE_2)
		return errorDescriptor
	}
}
