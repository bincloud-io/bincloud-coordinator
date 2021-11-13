package io.bce.validation;

import java.util.HashMap;
import java.util.Map;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@Getter
@EqualsAndHashCode
public final class ErrorMessage {
	private final String message;
	private final Map<String, Object> parameters = new HashMap<>();

	private ErrorMessage(String message, Map<String, Object> parameters) {
		super();
		this.message = message;
		this.parameters.putAll(parameters);
	}

	private ErrorMessage(ErrorMessage proto) {
		this(proto.getMessage(), proto.getParameters());
	}

	/**
	 * Create the error message without pre-defined parameters
	 * 
	 * @param message The message text
	 * @return The error message
	 */
	public static final ErrorMessage createFor(@NonNull String message) {
		return createFor(message, new HashMap<>());
	}

	/**
	 * Create the error message with pre-defined parameters
	 * 
	 * @param message
	 * @param parameters
	 * @return The error message
	 */
	public static final ErrorMessage createFor(@NonNull String message, @NonNull Map<String, Object> parameters) {
		return new ErrorMessage(message, parameters);
	}

	@Override
	public String toString() {
		return message;
	}	
	
	/**
	 * Create error message with specified parameter
	 * 
	 * @param key   The parameter key
	 * @param value The parameter value
	 * @return The error message
	 */
	public final ErrorMessage withParameter(@NonNull String key, Object value) {
		ErrorMessage derived = new ErrorMessage(this);
		derived.putParameter(key, value);
		return derived;
	}

	private void putParameter(String key, Object value) {
		this.parameters.put(key, value);
	}
}
