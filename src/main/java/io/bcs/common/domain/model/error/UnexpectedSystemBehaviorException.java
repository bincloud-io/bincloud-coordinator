package io.bcs.common.domain.model.error;

public class UnexpectedSystemBehaviorException extends ApplicationException {
	private static final long serialVersionUID = 4129822450418450052L;

	public UnexpectedSystemBehaviorException(String context, Exception error) {
		super(Severity.INCIDENT, context, -1L, error.getMessage());
		initCause(error);
	}

	public UnexpectedSystemBehaviorException(Exception error) {
		this("GLOBAL", error);
	}
	
	@Override
	public String getMessage() {
		return getCause().getMessage();
	}
}
