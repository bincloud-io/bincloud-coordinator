package io.bcs.common.domain.model.validation;

import java.util.HashMap;
import java.util.Map;

import io.bcs.common.domain.model.error.ApplicationException;
import io.bcs.common.domain.model.validation.ValidationState.ErrorState;
import lombok.Getter;

@Getter
public class ValidationException extends ApplicationException {
	private static final long serialVersionUID = -2959637114905553972L;
	private static final String ERROR_MESSAGE = "Validatable object hasn't passed validation.";
	public static final String ERROR_STATE_PROPERTY = "$$errorState";
	public static final String WRONG_PARAMETERS_PROPERTY = "$wrongParameters";
	public static final String UNGROUPED_ERRORS_PROPERTY = "$ungroupedErrors";
	public static final String GROUPED_ERRORS_PROPERTY = "$groupedErrors";
	
	private final Map<String, Object> validationProperties;
	private final ErrorState errorState;

	public ValidationException(String context, Long errorNumber,
			ValidationState validationState) {
		super(Severity.BUSINESS, context, errorNumber, ERROR_MESSAGE);
		this.validationProperties = validationState.getAdditionalProperties();
		this.errorState = validationState.getErrorState();
	}
	
	@Override
	public Map<String, Object> getDetails() {
		Map<String, Object> details = new HashMap<String, Object>(super.getDetails());
		details.put(ERROR_STATE_PROPERTY, errorState);
		details.put(GROUPED_ERRORS_PROPERTY, errorState.getGroupedErrors());
		details.put(UNGROUPED_ERRORS_PROPERTY, errorState.getUngroupedErrors());
		details.put(WRONG_PARAMETERS_PROPERTY, errorState.getGroupedErrors().keySet());
		details.putAll(validationProperties);
		return details;
	}
}
