package io.bincloud.common.domain.model.io;

import io.bincloud.common.domain.model.error.ApplicationException;

public abstract class InputOutputException extends ApplicationException {
	private static final long serialVersionUID = 8769350037770641135L;
	public static final String CONTEXT = "COMMON__IO";
	
	protected InputOutputException(Long errorNumber, String message) {
		super(Severity.INCIDENT, CONTEXT, errorNumber, message);
	}
}
