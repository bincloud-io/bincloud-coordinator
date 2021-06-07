package io.bincloud.common.domain.model.logging.audit;

public interface ServiceAuditLogger {
	public void log(ServiceAuditEvent auditEvent);
}
