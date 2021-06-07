package io.bincloud.common.domain.model.logging.audit;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import io.bincloud.common.domain.model.error.ErrorDescriptor;
import io.bincloud.common.domain.model.logging.Level;
import io.bincloud.common.domain.model.logging.LogRecord;
import io.bincloud.common.domain.model.message.MessageTemplate;
import io.bincloud.common.domain.model.message.MessageTransformer;
import lombok.Getter;

public class ServiceAuditEvent {
	@Getter	
	private LogRecord auditLogRecord;
	private final AuditEventType auditEventType;
	private final Collection<String> auditParameterNames;

	public ServiceAuditEvent(Level auditLevel, ErrorDescriptor errorDescriptor, Collection<String> auditParameterNames) {
		this(new AuditEventType(errorDescriptor), new LogRecord(auditLevel, errorDescriptor), auditParameterNames);
	}

	public ServiceAuditEvent(String eventCode, Level auditLevel, MessageTemplate messageTemplate,
			Collection<String> auditParameterNames) {
		this(new AuditEventType(eventCode), new LogRecord(auditLevel, messageTemplate), auditParameterNames);
	}

	private ServiceAuditEvent(AuditEventType auditEvent, LogRecord auditLogRecord, Collection<String> auditParameterNames) {
		super();
		this.auditEventType = auditEvent;
		this.auditLogRecord = auditLogRecord;
		this.auditParameterNames = Collections.unmodifiableCollection(auditParameterNames);
	}

	private ServiceAuditEvent(ServiceAuditEvent proto) {
		super();
		this.auditEventType = proto.auditEventType;
		this.auditParameterNames = proto.auditParameterNames;
		this.auditLogRecord = proto.auditLogRecord;
	}
	
	public String getEventCode() {
		return auditEventType.getEventCode();
	}

	public Long getErrorCode() {
		return auditEventType.getErrorCode();
	}
	
	public Level getAuditLogLevel() {
		return auditLogRecord.getLevel();
	}

	public Instant getAuditLogTimestamp() {
		return auditLogRecord.getTimestamp();
	}

	public String getAuditLogMessageText() {
		return auditLogRecord.getMessageText();
	}
	
	public Map<String, String> getAuditDetailsParameters() {
		return getAuditLogMessageParameters().entrySet().stream()
				.filter(entry -> auditParameterNames.contains(entry.getKey()))
				.collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().toString()));
	}

	public ServiceAuditEvent transformMessage(MessageTransformer messageTransformer) {
		ServiceAuditEvent derived = new ServiceAuditEvent(this);
		derived.auditLogRecord = derived.auditLogRecord.transformMessage(messageTransformer);
		return derived;
	}
	
	private Map<String, Object> getAuditLogMessageParameters() {
		return auditLogRecord.getMessageParameters();
	}
}
