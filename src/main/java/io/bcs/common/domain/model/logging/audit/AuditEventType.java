package io.bcs.common.domain.model.logging.audit;

import io.bcs.common.domain.model.error.ErrorDescriptor;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AuditEventType {
	private static final Long SUCCESS_CODE = 0L;
	private final String eventCode;
	private final Long errorCode;

	public AuditEventType(String eventCode) {
		this(eventCode, SUCCESS_CODE);
	}

	public AuditEventType(ErrorDescriptor errorDescriptor) {
		this(errorDescriptor.getContext(), errorDescriptor.getErrorCode());
	}
}