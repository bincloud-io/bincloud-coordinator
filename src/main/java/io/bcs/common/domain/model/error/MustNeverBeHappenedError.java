package io.bcs.common.domain.model.error;

public class MustNeverBeHappenedError extends Error {
	private static final long serialVersionUID = 3498435159012071974L;

	public MustNeverBeHappenedError(String message) {
		super(message);
	}
	
	public MustNeverBeHappenedError(Throwable cause) {
		super(String.format("Error %s must never be happened for this case.", cause.getClass()), cause);
	}
}
