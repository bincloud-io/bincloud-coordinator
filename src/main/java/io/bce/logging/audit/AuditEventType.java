package io.bce.logging.audit;

import io.bce.domain.BoundedContextId;
import io.bce.domain.errors.ErrorDescriptor;
import io.bce.domain.errors.ErrorDescriptor.ErrorCode;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AuditEventType {
	private final BoundedContextId contextId;
	private final ErrorCode errorCode;

	public AuditEventType(BoundedContextId contextId) {
		this(contextId, ErrorCode.SUCCESSFUL_COMPLETED_CODE);
	}

	public AuditEventType(ErrorDescriptor errorDescriptor) {
		this(errorDescriptor.getContextId(), errorDescriptor.getErrorCode());
	}
}