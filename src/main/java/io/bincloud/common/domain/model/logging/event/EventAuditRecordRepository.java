package io.bincloud.common.domain.model.logging.event;

public interface EventAuditRecordRepository {
	public void save(EventAuditRecord auditRecord);
}
