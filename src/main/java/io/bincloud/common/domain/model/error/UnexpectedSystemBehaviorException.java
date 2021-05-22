package io.bincloud.common.domain.model.error;

import lombok.NonNull;

public class UnexpectedSystemBehaviorException extends ApplicationException {
	private static final long serialVersionUID = 4129822450418450052L;

	public UnexpectedSystemBehaviorException(@NonNull String context, @NonNull Exception error) {
		super(Severity.INCIDENT, context, -1L, error.getMessage());
		initCause(error);
	}

	public UnexpectedSystemBehaviorException(@NonNull Exception error) {
		this("GLOBAL", error);
	}
	
	@Override
	public String getMessage() {
		return getCause().getMessage();
	}
}
