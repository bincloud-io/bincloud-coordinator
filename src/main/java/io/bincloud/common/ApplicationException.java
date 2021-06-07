package io.bincloud.common;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class ApplicationException extends RuntimeException {
	private static final long serialVersionUID = -8702065273619734063L;
	private static final String ERROR_MESSAGE_TEMPLATE = "[%s] [%s__%04d] %s";
	private final Severity severity;
	private final String context;
	private final Long errorCode;
	
	public ApplicationException(
		@NonNull Severity severity, 
		@NonNull String context, 
		@NonNull Long errorNumber, 
		String message) {
		super(message);
		this.severity = severity;
		this.errorCode = errorNumber;
		this.context = context;
	}
	
	@Override
	public String getMessage() {
		return String
			.format(ERROR_MESSAGE_TEMPLATE, severity, context, errorCode, super.getMessage())
			.intern();
	}
	
	public enum Severity {
		INCIDENT,
		BUSINESS
	}

}
