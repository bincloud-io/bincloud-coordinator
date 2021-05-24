package io.bincloud.common.domain.model.logging;

public interface EventsAuditLogger {
	public void log(AuditableEvent auditEvent);
}
