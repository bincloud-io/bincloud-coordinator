package io.bce.logging.audit;

public interface ServiceAuditLogger {
  public void log(ServiceAuditEvent auditEvent);
}
