package io.bce.logging.audit;

import io.bce.domain.BoundedContextId;
import io.bce.domain.errors.ErrorDescriptor;
import io.bce.domain.errors.ErrorDescriptor.ErrorCode;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This class represents the audit event type.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AuditEventType {
  private final BoundedContextId contextId;
  private final ErrorCode errorCode;

  /**
   * Create audit event type for specified context.
   *
   * @param contextId The bounded context id
   */
  public AuditEventType(BoundedContextId contextId) {
    this(contextId, ErrorCode.SUCCESSFUL_COMPLETED_CODE);
  }

  /**
   * Create audit event type for specified error descriptor.
   *
   * @param errorDescriptor The error descriptor
   */
  public AuditEventType(ErrorDescriptor errorDescriptor) {
    this(errorDescriptor.getContextId(), errorDescriptor.getErrorCode());
  }
}