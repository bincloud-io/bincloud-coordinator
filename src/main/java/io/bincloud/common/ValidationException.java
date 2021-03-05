package io.bincloud.common;

import io.bincloud.common.ValidationState.ErrorState;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class ValidationException extends ApplicationException {
	private static final long serialVersionUID = -2959637114905553972L;
	public static final String VALIDATION_CONTEXT = "COMMON__VALIDATION";
	private static final String ERROR_MESSAGE = "Validatable object hasn't passed validation.";
	
	private ErrorState errorState;
	
	public ValidationException(@NonNull Severity severity, @NonNull ValidationState validationState) {
		super(severity, VALIDATION_CONTEXT, 1L, ERROR_MESSAGE);
		this.errorState = validationState.getErrorState();
	}
}
