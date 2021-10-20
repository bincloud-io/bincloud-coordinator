package io.bcs.common.domain.model.io;

import io.bce.domain.BoundedContextId;
import io.bce.domain.errors.ApplicationException;

public abstract class InputOutputException extends ApplicationException {
	private static final long serialVersionUID = 8769350037770641135L;
	public static final BoundedContextId CONTEXT = BoundedContextId.createFor("COMMON__IO");
	
	protected InputOutputException(ErrorCode errorNumber, String message) {
		super(CONTEXT, ErrorSeverity.INCIDENT, errorNumber, message);
	}
}
