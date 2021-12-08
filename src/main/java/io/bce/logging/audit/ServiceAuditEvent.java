package io.bce.logging.audit;

import io.bce.domain.BoundedContextId;
import io.bce.domain.errors.ErrorDescriptor;
import io.bce.domain.errors.ErrorDescriptor.ErrorCode;
import io.bce.logging.Level;
import io.bce.logging.LogRecord;
import io.bce.text.TextTemplate;
import io.bce.text.TextTemplate.Transformer;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 * Create the service audit event.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class ServiceAuditEvent {
  @Getter
  private LogRecord auditLogRecord;
  private final AuditEventType auditEventType;
  private final Collection<String> auditParameterNames;

  /**
   * Create the service audit event for the error descriptor.
   *
   * @param auditLevel          The log level
   * @param errorDescriptor     The error descriptor
   * @param auditParameterNames The audit parameter names, passed to the event
   */
  public ServiceAuditEvent(Level auditLevel, ErrorDescriptor errorDescriptor,
      Collection<String> auditParameterNames) {
    this(new AuditEventType(errorDescriptor), new LogRecord(auditLevel, errorDescriptor),
        auditParameterNames);
  }

  /**
   * Create the service audit event for the error descriptor.
   *
   * @param contextId           The context id
   * @param auditLevel          The log level
   * @param messageTemplate     The message text template
   * @param auditParameterNames The audit parameter names, passed to the event
   */
  public ServiceAuditEvent(BoundedContextId contextId, Level auditLevel,
      TextTemplate messageTemplate, Collection<String> auditParameterNames) {
    this(new AuditEventType(contextId), new LogRecord(auditLevel, messageTemplate),
        auditParameterNames);
  }

  private ServiceAuditEvent(AuditEventType auditEvent, LogRecord auditLogRecord,
      Collection<String> auditParameterNames) {
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

  /**
   * Get the bounded context id.
   *
   * @return The bounded context id
   */
  public BoundedContextId getContextId() {
    return auditEventType.getContextId();
  }

  /**
   * Get the error code.
   *
   * @return The error code
   */
  public ErrorCode getErrorCode() {
    return auditEventType.getErrorCode();
  }

  /**
   * Get the audit log level.
   *
   * @return The audit log level
   */
  public Level getAuditLogLevel() {
    return auditLogRecord.getLevel();
  }

  /**
   * Get the audit log timestamp.
   *
   * @return The audit log timestamp
   */
  public Instant getAuditLogTimestamp() {
    return auditLogRecord.getTimestamp();
  }

  /**
   * Get the audit log message text.
   *
   * @return The audit log message text.
   */
  public String getAuditLogMessageText() {
    return auditLogRecord.getMessageText();
  }

  /**
   * Get parameters for audit.
   *
   * @return The parameters for audit
   */
  public Map<String, String> getAuditDetailsParameters() {
    return getAuditLogMessageParameters().entrySet().stream()
        .filter(entry -> auditParameterNames.contains(entry.getKey()))
        .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().toString()));
  }

  /**
   * Transform the service audit event description.
   *
   * @param messageTransformer The message transformer
   * @return The transformed event
   */
  public ServiceAuditEvent transformMessage(Transformer messageTransformer) {
    ServiceAuditEvent derived = new ServiceAuditEvent(this);
    derived.auditLogRecord = derived.auditLogRecord.transformMessage(messageTransformer);
    return derived;
  }

  private Map<String, Object> getAuditLogMessageParameters() {
    return auditLogRecord.getMessageParameters();
  }
}
