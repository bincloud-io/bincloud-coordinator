package io.bcs.common.domain.model.logging.audit;

public interface ServiceAuditLogger {
	public void log(ServiceAuditEvent auditEvent);
}
