package io.bincloud.common.domain.model.logging;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import io.bincloud.common.domain.model.error.ErrorDescriptor;
import io.bincloud.common.domain.model.message.MessageTemplate;
import io.bincloud.common.domain.model.message.MessageTransformer;
import lombok.Getter;

public class AuditableEvent {
	@Getter	
	private LogRecord auditLogRecord;
	private final AuditableEventType auditEventType;
	private final Collection<String> auditParameterNames;

	public AuditableEvent(Level auditLevel, ErrorDescriptor errorDescriptor, Collection<String> auditParameterNames) {
		this(new AuditableEventType(errorDescriptor), new LogRecord(auditLevel, errorDescriptor), auditParameterNames);
	}

	public AuditableEvent(String eventCode, Level auditLevel, MessageTemplate messageTemplate,
			Collection<String> auditParameterNames) {
		this(new AuditableEventType(eventCode), new LogRecord(auditLevel, messageTemplate), auditParameterNames);
	}

	private AuditableEvent(AuditableEventType auditEvent, LogRecord auditLogRecord, Collection<String> auditParameterNames) {
		super();
		this.auditEventType = auditEvent;
		this.auditLogRecord = auditLogRecord;
		this.auditParameterNames = Collections.unmodifiableCollection(auditParameterNames);
	}

	private AuditableEvent(AuditableEvent proto) {
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

	public AuditableEvent transformMessage(MessageTransformer messageTransformer) {
		AuditableEvent derived = new AuditableEvent(this);
		derived.auditLogRecord = derived.auditLogRecord.transformMessage(messageTransformer);
		return derived;
	}
	
	private Map<String, Object> getAuditLogMessageParameters() {
		return auditLogRecord.getMessageParameters();
	}
}
