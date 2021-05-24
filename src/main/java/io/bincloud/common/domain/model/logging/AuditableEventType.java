package io.bincloud.common.domain.model.logging;

import io.bincloud.common.domain.model.error.ErrorDescriptor;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AuditableEventType {
	private static final Long SUCCESS_CODE = 0L;
	private final String eventCode;
	private final Long errorCode;

	public AuditableEventType(String eventCode) {
		this(eventCode, SUCCESS_CODE);
	}

	public AuditableEventType(ErrorDescriptor errorDescriptor) {
		this(errorDescriptor.getContext(), errorDescriptor.getErrorCode());
	}
}